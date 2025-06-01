package fincontrol.com.fincontrol.controller;

import fincontrol.com.fincontrol.dto.ExtraIncomeDto;
import fincontrol.com.fincontrol.service.ExtraIncomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // <<< ADICIONE ESTE IMPORT
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
// import fincontrol.com.fincontrol.security.UserDetailsImpl; // Descomente se UserDetailsImpl existir e for usado

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Tag(name = "Renda Extra")
@RestController
@RequestMapping("/api/extra-incomes")
@RequiredArgsConstructor
@Slf4j // <<< ADICIONE ESTA ANOTAÇÃO
public class ExtraIncomeController {

    private final ExtraIncomeService incomeService;

    @Operation(summary = "Criar nova renda extra")
    @ApiResponse(responseCode = "201", description = "Renda extra criada com sucesso",
            content = @Content(schema = @Schema(implementation = ExtraIncomeDto.class)))
    @PostMapping
    public ResponseEntity<ExtraIncomeDto> createIncome(
            @Valid @RequestBody ExtraIncomeDto dto,
            Principal principal) {
        UUID userId = getUserIdFromPrincipal(principal);
        ExtraIncomeDto createdDto = incomeService.createIncome(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDto);
    }

    @Operation(summary = "Listar rendas do usuário")
    @ApiResponse(responseCode = "200", description = "Lista de rendas extras retornada com sucesso",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ExtraIncomeDto.class))))
    @GetMapping
    public ResponseEntity<List<ExtraIncomeDto>> listUserIncomes(Principal principal) {
        UUID userId = getUserIdFromPrincipal(principal);
        List<ExtraIncomeDto> dtoList = incomeService.getIncomesByUser(userId);
        return ResponseEntity.ok(dtoList);
    }

    @Operation(summary = "Listar rendas por banco")
    @ApiResponse(responseCode = "200", description = "Lista de rendas extras por banco retornada com sucesso",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ExtraIncomeDto.class))))
    @GetMapping("/by-bank/{bankId}")
    public ResponseEntity<List<ExtraIncomeDto>> listIncomesByBank(
            @PathVariable UUID bankId,
            Principal principal) {
        UUID userId = getUserIdFromPrincipal(principal);
        List<ExtraIncomeDto> dtoList = incomeService.getIncomesByBankAndUser(bankId, userId);
        return ResponseEntity.ok(dtoList);
    }

    private UUID getUserIdFromPrincipal(Principal principal) {
        if (principal == null) {
            // Este log não será alcançado se a exceção for lançada antes.
            // Se você quiser logar antes de lançar a exceção, mova o log para cima.
            log.error("Principal é nulo. Acesso não autenticado ou problema na configuração de segurança.");
            throw new IllegalStateException("Principal não pode ser nulo. Verifique a configuração de segurança e a autenticação.");
        }

        if (principal instanceof Authentication) {
            Authentication authentication = (Authentication) principal;
            Object principalDetails = authentication.getPrincipal();

            if (principalDetails instanceof String) {
                String userIdString = (String) principalDetails;
                if ("anonymousUser".equals(userIdString)) {
                    log.warn("Tentativa de acesso por usuário anônimo bloqueada.");
                    throw new IllegalStateException("Acesso anônimo não permitido para esta operação.");
                }
                try {
                    return UUID.fromString(userIdString);
                } catch (IllegalArgumentException e) {
                    log.error("O principal (String) não é um UUID válido: '{}'", userIdString, e);
                    throw new IllegalStateException("O principal (String) obtido da autenticação não é um UUID válido: " + userIdString, e);
                }
                // } else if (principalDetails instanceof UserDetailsImpl) { // Exemplo se você usa um UserDetails customizado
                // UserDetailsImpl userDetails = (UserDetailsImpl) principalDetails;
                // return userDetails.getId(); // Supondo que UserDetailsImpl tenha getId()
            } else {
                String detailsType = (principalDetails != null) ? principalDetails.getClass().getName() : "null";
                log.warn("Detalhes do principal não são do tipo String (UUID) esperado ou UserDetailsImpl customizado. Tipo encontrado: {}", detailsType);
                try {
                    // Tentar authentication.getName() como fallback. Certifique-se que isso retorna o UUID.
                    return UUID.fromString(authentication.getName());
                } catch (IllegalArgumentException e) {
                    log.error("Falha ao converter authentication.getName() ('{}') para UUID.", authentication.getName(), e);
                    throw new IllegalStateException("Não foi possível determinar o UUID do usuário a partir do Principal. Detalhes do tipo: " + detailsType, e);
                }
            }
        } else {
            // Este caso é menos provável com Spring Security configurado corretamente.
            log.warn("Principal não é uma instância de Authentication. Tipo do Principal: {}", principal.getClass().getName());
            try {
                return UUID.fromString(principal.getName());
            } catch (Exception e) {
                log.error("Não foi possível extrair o UUID do usuário do Principal (tipo: {}, nome: '{}')", principal.getClass().getName(), principal.getName(), e);
                throw new IllegalStateException("Não foi possível extrair o UUID do usuário do Principal (tipo: " + principal.getClass().getName() + ", nome: " + principal.getName() + ")", e);
            }
        }
    }
}