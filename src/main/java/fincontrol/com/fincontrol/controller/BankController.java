package fincontrol.com.fincontrol.controller;

import fincontrol.com.fincontrol.dto.BankCreateDto;
import fincontrol.com.fincontrol.dto.BankDto;
import fincontrol.com.fincontrol.dto.BankUpdateDto;
import fincontrol.com.fincontrol.service.BankService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid; // Import for @Valid if you use it on DTOs
import java.net.URI; // For creating location URI
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/banks")
@Tag(name = "Bancos", description = "Gerenciamento de bancos")
public class BankController {
    private final BankService service;

    public BankController(BankService s) {
        this.service = s;
    }

    @Operation(summary = "Cria um novo banco")
    @ApiResponse(responseCode = "201", description = "Banco criado com sucesso",
            content = @Content(schema = @Schema(implementation = BankDto.class)))
    @PostMapping
    public ResponseEntity<BankDto> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do banco a criar",
                    required = true,
                    content = @Content(schema = @Schema(implementation = BankCreateDto.class))
            )
            @Valid @RequestBody BankCreateDto dto) { // Added @Valid assuming you might want validation
        BankDto createdBank = service.create(dto);
        // Optionally, set Location header
        URI location = URI.create("/api/banks/" + createdBank.getId());
        return ResponseEntity.created(location).body(createdBank);
    }

    @Operation(summary = "Lista todos os bancos")
    @ApiResponse(responseCode = "200", description = "Lista de bancos retornada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BankDto.class)))
    @GetMapping
    public List<BankDto> list() {
        return service.listAll();
    }

    @Operation(summary = "Atualiza um banco existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Banco atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = BankDto.class))),
            @ApiResponse(responseCode = "404", description = "Banco não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BankDto> update(
            @Parameter(description = "ID do banco a atualizar", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Novos dados do banco",
                    required = true,
                    content = @Content(schema = @Schema(implementation = BankUpdateDto.class))
            )
            @Valid @RequestBody BankUpdateDto dto) { // Added @Valid
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Remove um banco por ID (e todas as suas receitas e despesas associadas)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Banco e dados associados removidos com sucesso"),
            @ApiResponse(responseCode = "404", description = "Banco não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAll(
            @Parameter(description = "ID do banco a remover", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id) {
        service.deleteAll(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove todas as receitas associadas a um banco")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Receitas do banco removidas com sucesso"),
            @ApiResponse(responseCode = "404", description = "Banco não encontrado")
    })
    @DeleteMapping("/{id}/clear-incomes")
    public ResponseEntity<Void> clearIncomes(
            @Parameter(description = "ID do banco para limpar as receitas", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id) {
        service.clearIncomes(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove todas as despesas associadas a um banco")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Despesas do banco removidas com sucesso"),
            @ApiResponse(responseCode = "404", description = "Banco não encontrado")
    })
    @DeleteMapping("/{id}/clear-expenses")
    public ResponseEntity<Void> clearExpenses(
            @Parameter(description = "ID do banco para limpar as despesas", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id) {
        service.clearExpenses(id);
        return ResponseEntity.noContent().build();
    }
}