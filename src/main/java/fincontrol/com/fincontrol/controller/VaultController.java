package fincontrol.com.fincontrol.controller;

import fincontrol.com.fincontrol.dto.*;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException; // Se usar
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.repository.UserRepository;
import fincontrol.com.fincontrol.service.VaultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vaults")
@Tag(name = "Cofres", description = "Gerenciamento de cofres do usuário")
public class VaultController {

    private final VaultService vaultService;
    private final UserRepository userRepository; // Para obter o ID do usuário

    public VaultController(VaultService vaultService, UserRepository userRepository) {
        this.vaultService = vaultService;
        this.userRepository = userRepository;
    }

    private UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            // Idealmente, isso não deveria ser alcançado se o filtro JWT protegeu a rota
            throw new ResourceNotFoundException("Nenhum usuário autenticado encontrado no contexto de segurança.");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof String)) {
            // Isso indica uma inconsistência entre o que o filtro JWT define como principal
            // e o que este método espera.
            throw new IllegalStateException("O principal da autenticação não é uma String (UserID) como esperado. Principal é do tipo: " + principal.getClass().getName());
        }

        String userIdString = (String) principal;

        // Verificação adicional, embora o filtro JWT deva idealmente lidar com tokens inválidos antes disso.
        if ("anonymousUser".equals(userIdString)) {
            throw new ResourceNotFoundException("Operação não permitida para usuário anônimo.");
        }

        try {
            return UUID.fromString(userIdString);
        } catch (IllegalArgumentException e) {
            // Isso aconteceria se o principal (vindo do token) não fosse um UUID válido em formato string.
            throw new ResourceNotFoundException("ID do usuário autenticado ('" + userIdString + "') não é um UUID válido.");
        }
    }

    @Operation(summary = "Cria um novo cofre para o usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cofre criado com sucesso", content = @Content(schema = @Schema(implementation = VaultDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou saldo insuficiente no banco"),
            @ApiResponse(responseCode = "404", description = "Usuário ou Banco não encontrado")
    })
    @PostMapping
    public ResponseEntity<VaultDto> createVault(@Valid @RequestBody VaultCreateDto vaultCreateDto) {
        UUID userId = getAuthenticatedUserId();
        VaultDto createdVault = vaultService.createVault(vaultCreateDto, userId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdVault.getId()).toUri();
        return ResponseEntity.created(location).body(createdVault);
    }

    @Operation(summary = "Lista todos os cofres do usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Lista de cofres retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<VaultDto>> getAllVaultsForUser() {
        UUID userId = getAuthenticatedUserId();
        List<VaultDto> vaults = vaultService.getAllVaultsByUser(userId);
        return ResponseEntity.ok(vaults);
    }

    @Operation(summary = "Busca um cofre específico pelo ID para o usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cofre encontrado", content = @Content(schema = @Schema(implementation = VaultDto.class))),
            @ApiResponse(responseCode = "404", description = "Cofre não encontrado ou não pertence ao usuário")
    })
    @GetMapping("/{vaultId}")
    public ResponseEntity<VaultDto> getVaultById(
            @Parameter(description = "ID do cofre a ser buscado") @PathVariable UUID vaultId) {
        UUID userId = getAuthenticatedUserId();
        VaultDto vault = vaultService.getVaultByIdAndUser(vaultId, userId);
        return ResponseEntity.ok(vault);
    }

    @Operation(summary = "Lista todos os cofres do usuário autenticado vinculados a um banco específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de cofres retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Banco não encontrado ou não pertence ao usuário")
    })
    @GetMapping("/bank/{bankId}")
    public ResponseEntity<List<VaultDto>> getVaultsByBank(
            @Parameter(description = "ID do banco para filtrar os cofres") @PathVariable UUID bankId) {
        UUID userId = getAuthenticatedUserId();
        List<VaultDto> vaults = vaultService.getVaultsByBankAndUser(bankId, userId);
        return ResponseEntity.ok(vaults);
    }


    @Operation(summary = "Atualiza dados de um cofre existente do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cofre atualizado com sucesso", content = @Content(schema = @Schema(implementation = VaultDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Cofre não encontrado ou não pertence ao usuário")
    })
    @PutMapping("/{vaultId}")
    public ResponseEntity<VaultDto> updateVault(
            @Parameter(description = "ID do cofre a ser atualizado") @PathVariable UUID vaultId,
            @Valid @RequestBody VaultUpdateDto vaultUpdateDto) {
        UUID userId = getAuthenticatedUserId();
        VaultDto updatedVault = vaultService.updateVault(vaultId, vaultUpdateDto, userId);
        return ResponseEntity.ok(updatedVault);
    }

    @Operation(summary = "Deleta um cofre do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cofre deletado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Operação inválida (ex: tentar deletar cofre não vinculado a banco com saldo)"),
            @ApiResponse(responseCode = "404", description = "Cofre não encontrado ou não pertence ao usuário")
    })
    @DeleteMapping("/{vaultId}")
    public ResponseEntity<Void> deleteVault(
            @Parameter(description = "ID do cofre a ser deletado") @PathVariable UUID vaultId) {
        UUID userId = getAuthenticatedUserId();
        vaultService.deleteVault(vaultId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Realiza um saque de um cofre específico do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saque realizado com sucesso", content = @Content(schema = @Schema(implementation = VaultTransactionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Valor de saque inválido ou saldo insuficiente no cofre"),
            @ApiResponse(responseCode = "404", description = "Cofre não encontrado ou não pertence ao usuário")
    })
    @PostMapping("/{vaultId}/withdraw")
    public ResponseEntity<VaultTransactionResponseDto> withdrawFromVault(
            @Parameter(description = "ID do cofre de onde sacar") @PathVariable UUID vaultId,
            @Valid @RequestBody VaultTransactionRequestDto transactionDto) {
        UUID userId = getAuthenticatedUserId();
        VaultTransactionResponseDto response = vaultService.withdrawFromVault(vaultId, transactionDto, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Realiza um depósito em um cofre específico do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Depósito realizado com sucesso", content = @Content(schema = @Schema(implementation = VaultTransactionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Valor de depósito inválido ou saldo insuficiente no banco vinculado (se houver)"),
            @ApiResponse(responseCode = "404", description = "Cofre ou banco vinculado não encontrado ou não pertence ao usuário")
    })
    @PostMapping("/{vaultId}/deposit")
    public ResponseEntity<VaultTransactionResponseDto> depositToVault(
            @Parameter(description = "ID do cofre onde depositar") @PathVariable UUID vaultId,
            @Valid @RequestBody VaultTransactionRequestDto transactionDto) {
        UUID userId = getAuthenticatedUserId();
        VaultTransactionResponseDto response = vaultService.depositToVault(vaultId, transactionDto, userId);
        return ResponseEntity.ok(response);
    }

}