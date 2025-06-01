package fincontrol.com.fincontrol.controller;

import fincontrol.com.fincontrol.dto.ErrorResponseDto;
import fincontrol.com.fincontrol.dto.LoginDto;
import fincontrol.com.fincontrol.dto.UserDto;
import fincontrol.com.fincontrol.dto.UserRegisterDto;
import fincontrol.com.fincontrol.exception.InvalidOperationException;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.security.JwtTokenProvider;
import fincontrol.com.fincontrol.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints de registro e login de usuários")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    public AuthController(UserService userService,
                          JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
    }

    @Operation(summary = "Registra um novo usuário no sistema",
            description = "Cria um novo usuário com base nos dados fornecidos. Validações incluem formato de nome, e-mail único, força da senha, confirmação de senha e salário não negativo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description =
                    "**Dados de registro inválidos.** Pode ocorrer devido a: <br>" +
                            "- Campos obrigatórios faltando (nome, email, senha, confirmarSenha, salario).<br>" +
                            "- **Nome:** Não informado, menos de duas palavras, caracteres inválidos, ou fora do tamanho permitido (5-100 caracteres).<br>" +
                            "- **Email:** Formato inválido ou tamanho excedido.<br>" +
                            "- **Senha:** Menos de 6 caracteres, não contém letras e números.<br>" +
                            "- **Confirmar Senha:** Não confere com a senha informada.<br>" +
                            "- **Salário:** Valor negativo.<br>" +
                            "A resposta incluirá um campo 'details' com a lista específica de erros de validação.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = {
                                    @ExampleObject(name = "Erro de Validação Múltipla", summary = "Exemplo com múltiplos erros de campo",
                                            value = "{\"timestamp\":\"2025-05-30T22:00:00Z\",\"status\":400,\"error\":\"Erro de Validação de Campo\",\"message\":\"Um ou mais campos falharam na validação. Veja os detalhes.\",\"path\":\"/auth/register\",\"details\":[\"name: Você precisa informar o seu nome completo com ao menos duas palavras, e cada palavra deve ter no mínimo 2 letras (somente letras e apóstrofos são permitidos).\",\"password: A sua senha está fraca, ela precisa possuir no mínimo 6 caracteres, com ao menos uma letra e um número.\"]}"),
                                    @ExampleObject(name = "Erro de Senhas Não Conferem", summary = "Exemplo de senhas não conferem",
                                            value = "{\"timestamp\":\"2025-05-30T22:01:00Z\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"As duas senhas não conferem, elas precisam ter os mesmos caracteres exatamente iguais.\",\"path\":\"/auth/register\"}")
                            })),
            @ApiResponse(responseCode = "409", description = "Conflito - O e-mail fornecido já está cadastrado no sistema.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "E-mail Duplicado", summary = "Exemplo de e-mail já existente",
                                    value = "{\"timestamp\":\"2025-05-30T22:02:00Z\",\"status\":409,\"error\":\"Conflict\",\"message\":\"O e-mail 'existente@example.com' já está cadastrado.\",\"path\":\"/auth/register\"}")))
    })
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para registro de um usuário. Todos os campos são validados conforme as regras de negócio.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRegisterDto.class))
            )
            @Valid @RequestBody UserRegisterDto dto
    ) {
        if (userService.findByEmail(dto.getEmail()).isPresent()) {
            throw new InvalidOperationException("O e-mail '" + dto.getEmail() + "' já está cadastrado.");
        }
        User registeredUser = userService.register(dto);

        UserDto userDto = new UserDto(
                registeredUser.getId(),
                registeredUser.getName(),
                registeredUser.getEmail(),
                registeredUser.getSalary(),
                registeredUser.getCreatedAt(),
                registeredUser.getUpdatedAt()
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(registeredUser.getId())
                .toUri();

        return ResponseEntity.created(location).body(userDto);
    }

    @Operation(summary = "Autentica um usuário e retorna um token JWT",
            description = "Valida as credenciais do usuário (e-mail e senha). Em caso de sucesso, retorna um token JWT para ser usado em requisições autenticadas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticado com sucesso. O token JWT é retornado no corpo da resposta.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"token\": \"Bearer seu.jwt.token.aqui\", \"userId\": \"uuid-do-usuario\", \"userName\": \"Nome do Usuario\"}"))),
            @ApiResponse(responseCode = "400", description = "Dados de login inválidos (e-mail ou senha não fornecidos ou em formato incorreto).",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "Campo Faltando", summary = "Exemplo de e-mail não fornecido",
                                    value = "{\"timestamp\":\"2025-05-30T22:03:00Z\",\"status\":400,\"error\":\"Erro de Validação de Campo\",\"message\":\"Um ou mais campos falharam na validação. Veja os detalhes.\",\"path\":\"/auth/login\",\"details\":[\"email: O e-mail é obrigatório.\"]}"))),
            @ApiResponse(responseCode = "401", description = "Não autorizado - Credenciais inválidas (senha incorreta).",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "Senha Incorreta", summary = "Exemplo de senha incorreta",
                                    value = "{\"timestamp\":\"2025-05-30T22:04:00Z\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"A sua senha está incorreta.\",\"path\":\"/auth/login\"}"))),
            @ApiResponse(responseCode = "404", description = "Não encontrado - O e-mail fornecido não está cadastrado no sistema.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "E-mail Não Encontrado", summary = "Exemplo de e-mail não cadastrado",
                                    value = "{\"timestamp\":\"2025-05-30T22:05:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"O email 'naoexiste@example.com' não está cadastrado no sistema.\",\"path\":\"/auth/login\"}")))
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciais de acesso do usuário (e-mail e senha).",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginDto.class))
            )
            @Valid @RequestBody LoginDto dto
    ) {
        User u = userService.authenticate(dto);
        // Chamada corrigida para incluir o ID do usuário
        String token = tokenProvider.generateToken(u.getEmail(), u.getId());
        return ResponseEntity.ok(Map.of("token", "Bearer " + token, "userId", u.getId().toString(), "userName", u.getName()));
    }
}