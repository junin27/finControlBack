package fincontrol.com.fincontrol.controller;

import fincontrol.com.fincontrol.dto.*;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.service.BankService;
import fincontrol.com.fincontrol.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/banks")
@Tag(name = "Bancos do Usuário", description = "Gerenciamento de bancos do usuário autenticado")
public class BankController {

    private final BankService service;
    private final UserService userService;

    public BankController(BankService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    /**
     * Interpreta o principal recebido (uma String que agora é o ID do usuário em UUID),
     * carrega o User correspondente e redefine o contexto de autenticação para usar esse UUID.
     */
    private void initUserContext(String principal) {
        UUID userId;
        try {
            userId = UUID.fromString(principal);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Identificador de usuário inválido no token: " + principal);
        }

        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário autenticado não encontrado com ID: " + userId));
        // Não é necessário alterar o SecurityContext aqui, pois o filtro já definiu principal=UUID.
    }

    @Operation(summary = "Cria um novo banco para o usuário autenticado")
    @ApiResponse(responseCode = "201", description = "Banco criado com sucesso",
            content = @Content(schema = @Schema(implementation = BankDto.class)))
    @PostMapping
    public ResponseEntity<BankDto> create(
            @Valid @RequestBody BankCreateDto dto,
            @AuthenticationPrincipal String principal
    ) {
        initUserContext(principal);
        BankDto createdBank = service.create(dto);
        URI location = URI.create("/api/banks/" + createdBank.getId());
        return ResponseEntity.created(location).body(createdBank);
    }

    @Operation(summary = "Lista todos os bancos do usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Lista de bancos retornada com sucesso",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BankDto.class))))
    @GetMapping
    public ResponseEntity<List<BankDto>> listAllUserBanks(
            @AuthenticationPrincipal String principal
    ) {
        initUserContext(principal);
        return ResponseEntity.ok(service.listAll());
    }

    @Operation(summary = "Atualiza um banco existente do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Banco atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = BankDto.class))),
            @ApiResponse(responseCode = "404", description = "Banco não encontrado ou não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = fincontrol.com.fincontrol.dto.ErrorResponseDto.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<BankDto> update(
            @Parameter(description = "ID do banco a atualizar", required = true,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id,
            @Valid @RequestBody BankUpdateDto dto,
            @AuthenticationPrincipal String principal
    ) {
        initUserContext(principal);
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Remove um banco específico do usuário (e todas as suas receitas e despesas associadas)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Banco e dados associados removidos com sucesso"),
            @ApiResponse(responseCode = "404", description = "Banco não encontrado ou não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = fincontrol.com.fincontrol.dto.ErrorResponseDto.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOneBank(
            @Parameter(description = "ID do banco a remover", required = true,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id,
            @AuthenticationPrincipal String principal
    ) {
        initUserContext(principal);
        service.deleteAll(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove todas as receitas associadas a um banco específico do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Receitas do banco removidas com sucesso"),
            @ApiResponse(responseCode = "404", description = "Banco não encontrado ou não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = fincontrol.com.fincontrol.dto.ErrorResponseDto.class)))
    })
    @DeleteMapping("/{id}/clear-incomes")
    public ResponseEntity<Void> clearIncomes(
            @Parameter(description = "ID do banco para limpar as receitas", required = true,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id,
            @AuthenticationPrincipal String principal
    ) {
        initUserContext(principal);
        service.clearIncomes(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove todas as despesas associadas a um banco específico do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Despesas do banco removidas com sucesso"),
            @ApiResponse(responseCode = "404", description = "Banco não encontrado ou não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = fincontrol.com.fincontrol.dto.ErrorResponseDto.class)))
    })
    @DeleteMapping("/{id}/clear-expenses")
    public ResponseEntity<Void> clearExpenses(
            @Parameter(description = "ID do banco para limpar as despesas", required = true,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id,
            @AuthenticationPrincipal String principal
    ) {
        initUserContext(principal);
        service.clearExpenses(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Realiza uma transferência de valor entre dois bancos do usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transferência realizada com sucesso.",
                    content = @Content(schema = @Schema(implementation = BankTransferResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (ex: valor inválido, saldo insuficiente, bancos iguais).",
                    content = @Content(schema = @Schema(implementation = fincontrol.com.fincontrol.dto.ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Um ou ambos os bancos não encontrados ou não pertencem ao usuário.",
                    content = @Content(schema = @Schema(implementation = fincontrol.com.fincontrol.dto.ErrorResponseDto.class)))
    })
    @PostMapping("/transfer")
    public ResponseEntity<BankTransferResponseDto> transferFunds(
            @Valid @RequestBody BankTransferDto transferDto,
            @AuthenticationPrincipal String principal
    ) {
        initUserContext(principal);
        BankTransferResponseDto response = service.transferBetweenBanks(transferDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualiza informações de múltiplos bancos do usuário em uma única requisição.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bancos atualizados com sucesso.",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BankDto.class)))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida.",
                    content = @Content(schema = @Schema(implementation = fincontrol.com.fincontrol.dto.ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Um ou mais bancos não encontrados.",
                    content = @Content(schema = @Schema(implementation = fincontrol.com.fincontrol.dto.ErrorResponseDto.class)))
    })
    @PutMapping("/update-all")
    public ResponseEntity<List<BankDto>> updateAllUserBanks(
            @Valid @RequestBody List<BankBulkUpdateItemDto> dtoList,
            @AuthenticationPrincipal String principal
    ) {
        initUserContext(principal);
        List<BankDto> updatedBanks = service.updateAllBanks(dtoList);
        return ResponseEntity.ok(updatedBanks);
    }

    @Operation(summary = "Exclui todos os bancos cadastrados pelo usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Todos os bancos do usuário foram excluídos com sucesso."),
            @ApiResponse(responseCode = "500", description = "Erro interno ao tentar excluir os bancos.",
                    content = @Content(schema = @Schema(implementation = fincontrol.com.fincontrol.dto.ErrorResponseDto.class)))
    })
    @DeleteMapping("/delete-all")
    public ResponseEntity<Void> deleteAllUserBanks(
            @AuthenticationPrincipal String principal
    ) {
        initUserContext(principal);
        service.deleteAllUserBanks();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca e retorna os dados de um banco específico do usuário pelo seu ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados do banco retornados com sucesso.",
                    content = @Content(schema = @Schema(implementation = BankDto.class))),
            @ApiResponse(responseCode = "404", description = "Banco não encontrado ou não pertence ao usuário.",
                    content = @Content(schema = @Schema(implementation = fincontrol.com.fincontrol.dto.ErrorResponseDto.class)))
    })
    @GetMapping("/{bankId}")
    public ResponseEntity<BankDto> getBankById(
            @Parameter(description = "ID do banco a ser buscado.", required = true,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID bankId,
            @AuthenticationPrincipal String principal
    ) {
        initUserContext(principal);
        BankDto bankDto = service.getBankById(bankId);
        return ResponseEntity.ok(bankDto);
    }

    @Operation(summary = "Adiciona um valor monetário ao saldo de um banco específico do usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Valor adicionado com sucesso. Retorna o banco com saldo atualizado.",
                    content = @Content(schema = @Schema(implementation = BankDto.class))),
            @ApiResponse(responseCode = "400", description = "Valor inválido (e.g., menor ou igual a zero).",
                    content = @Content(schema = @Schema(implementation = fincontrol.com.fincontrol.dto.ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Banco não encontrado ou não pertence ao usuário.",
                    content = @Content(schema = @Schema(implementation = fincontrol.com.fincontrol.dto.ErrorResponseDto.class)))
    })
    @PostMapping("/{bankId}/add-money")
    public ResponseEntity<BankDto> addMoneyToBank(
            @Parameter(description = "ID do banco.", required = true) @PathVariable UUID bankId,
            @Valid @RequestBody AmountDto amountDto,
            @AuthenticationPrincipal String principal
    ) {
        initUserContext(principal);
        BankDto updatedBank = service.addMoneyToBank(bankId, amountDto);
        return ResponseEntity.ok(updatedBank);
    }

    @Operation(summary = "Adiciona um valor monetário ao saldo de todos os bancos cadastrados pelo usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Valor adicionado com sucesso a todos os bancos aplicáveis. Retorna a lista de bancos com saldos atualizados.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BankDto.class)))),
            @ApiResponse(responseCode = "400", description = "Valor inválido (e.g., menor ou igual a zero).",
                    content = @Content(schema = @Schema(implementation = fincontrol.com.fincontrol.dto.ErrorResponseDto.class)))
    })
    @PostMapping("/add-money-all")
    public ResponseEntity<List<BankDto>> addMoneyToAllBanks(
            @Valid @RequestBody AmountDto amountDto,
            @AuthenticationPrincipal String principal
    ) {
        initUserContext(principal);
        List<BankDto> updatedBanks = service.addMoneyToAllBanks(amountDto);
        return ResponseEntity.ok(updatedBanks);
    }

    @Operation(summary = "Remove um valor monetário do saldo de um banco específico do usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Valor removido com sucesso. Retorna o banco com saldo atualizado.",
                    content = @Content(schema = @Schema(implementation = BankDto.class))),
            @ApiResponse(responseCode = "400", description = "Valor inválido ou saldo insuficiente.",
                    content = @Content(schema = @Schema(implementation = fincontrol.com.fincontrol.dto.ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Banco não encontrado ou não pertence ao usuário.",
                    content = @Content(schema = @Schema(implementation = fincontrol.com.fincontrol.dto.ErrorResponseDto.class)))
    })
    @PostMapping("/{bankId}/remove-money")
    public ResponseEntity<BankDto> removeMoneyFromBank(
            @Parameter(description = "ID do banco.", required = true) @PathVariable UUID bankId,
            @Valid @RequestBody AmountDto amountDto,
            @AuthenticationPrincipal String principal
    ) {
        initUserContext(principal);
        BankDto updatedBank = service.removeMoneyFromBank(bankId, amountDto);
        return ResponseEntity.ok(updatedBank);
    }

    @Operation(summary = "Remove um valor monetário do saldo de todos os bancos cadastrados pelo usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Valor removido com sucesso dos bancos aplicáveis. Retorna a lista de bancos com saldos atualizados.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BankDto.class)))),
            @ApiResponse(responseCode = "400", description = "Valor inválido ou saldo insuficiente em um ou mais bancos.",
                    content = @Content(schema = @Schema(implementation = fincontrol.com.fincontrol.dto.ErrorResponseDto.class)))
    })
    @PostMapping("/remove-money-all")
    public ResponseEntity<List<BankDto>> removeMoneyFromAllBanks(
            @Valid @RequestBody AmountDto amountDto,
            @AuthenticationPrincipal String principal
    ) {
        initUserContext(principal);
        List<BankDto> updatedBanks = service.removeMoneyFromAllBanks(amountDto);
        return ResponseEntity.ok(updatedBanks);
    }

    @Operation(summary = "Consolida e retorna diversas métricas financeiras sobre os bancos do usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Métricas dos bancos retornadas com sucesso.",
                    content = @Content(schema = @Schema(implementation = BankMetricsDto.class)))
    })
    @GetMapping("/metrics")
    public ResponseEntity<BankMetricsDto> getBankMetrics(
            @AuthenticationPrincipal String principal
    ) {
        initUserContext(principal);
        BankMetricsDto metrics = service.getBankMetrics();
        return ResponseEntity.ok(metrics);
    }
}
