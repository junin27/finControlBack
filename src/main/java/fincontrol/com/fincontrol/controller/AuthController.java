package fincontrol.com.fincontrol.controller;

import fincontrol.com.fincontrol.dto.LoginDto;
import fincontrol.com.fincontrol.dto.UserDto; // Importar UserDto para o retorno
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
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints de registro e login")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    public AuthController(UserService userService,
                          JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
    }

    @Operation(summary = "Registra um novo usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso",
                    content = @Content(schema = @Schema(implementation = UserDto.class))), // Retorna UserDto
            @ApiResponse(responseCode = "400", description = "Dados de registro inválidos (falha na validação do DTO ou senhas não conferem)"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
    })
    @PostMapping("/register")
    public ResponseEntity<UserDto> register( // Retorna UserDto
                                             @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                     description = "Dados para registro de um usuário",
                                                     required = true,
                                                     content = @Content(schema = @Schema(implementation = UserRegisterDto.class))
                                             )
                                             @Valid @RequestBody UserRegisterDto dto
    ) {
        if (userService.findByEmail(dto.getEmail()).isPresent()) {
            throw new InvalidOperationException("O e-mail '" + dto.getEmail() + "' já está cadastrado.");
        }
        User registeredUser = userService.register(dto);

        // Mapear para UserDto para não expor passwordHash
        UserDto userDto = new UserDto(
                registeredUser.getId(),
                registeredUser.getName(),
                registeredUser.getEmail(),
                registeredUser.getSalary(),
                registeredUser.getCreatedAt(),
                registeredUser.getUpdatedAt()
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}") // Embora este endpoint não exista, é comum para POST
                .buildAndExpand(registeredUser.getId())
                .toUri();

        return ResponseEntity.created(location).body(userDto);
    }

    @Operation(summary = "Autentica usuário e retorna token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado com sucesso",
                    content = @Content(schema = @Schema(type = "object", example = "{\"token\": \"Bearer seu.jwt.token\"}"))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas ou e-mail não encontrado")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciais de acesso",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginDto.class))
            )
            @Valid @RequestBody LoginDto dto
    ) {
        User u = userService.authenticate(dto);
        String token = tokenProvider.generateToken(u.getEmail());
        return ResponseEntity.ok(Map.of("token", "Bearer " + token, "userId", u.getId().toString(), "userName", u.getName()));
    }
}
