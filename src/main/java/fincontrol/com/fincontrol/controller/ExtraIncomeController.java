package fincontrol.com.fincontrol.controller;

import fincontrol.com.fincontrol.dto.*;
import fincontrol.com.fincontrol.service.ExtraIncomeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Tag(name = "ExtraIncome", description = "Endpoints para gerenciar Renda Extra (ExtraIncome)")
@RestController
@RequestMapping("/api/extra-incomes")
@Validated
public class ExtraIncomeController {

    private final ExtraIncomeService service;

    public ExtraIncomeController(ExtraIncomeService service) {
        this.service = service;
    }

    // 1. Criar nova renda extra
    @Operation(summary = "Cria uma nova Renda Extra para o usuário autenticado")
    @ApiResponse(responseCode = "201", description = "Renda Extra criada com sucesso",
            content = @Content(schema = @Schema(implementation = ExtraIncomeDto.class)))
    @PostMapping
    public ResponseEntity<ExtraIncomeDto> createExtraIncome(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para criação de uma nova Renda Extra",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ExtraIncomeCreateDto.class))
            )
            @Valid @RequestBody ExtraIncomeCreateDto dto
    ) {
        // Validações básicas podem ser movidas para o DTO com anotações de validação (e.g. @NotBlank, @NotNull)
        // No entanto, mantendo a lógica aqui para consistência com o código original.
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("O campo name é obrigatório.");
        }
        if (dto.getCategoryId() == null) {
            throw new IllegalArgumentException("O campo categoryId é obrigatório.");
        }
        if (dto.getAmount() == null) {
            throw new IllegalArgumentException("O campo amount é obrigatório.");
        }
        if (dto.getBankId() == null) {
            throw new IllegalArgumentException("O campo bankId é obrigatório.");
        }
        if (dto.getDate() == null) {
            throw new IllegalArgumentException("O campo date é obrigatório.");
        }
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            dto.setDescription("Campo não Informado pelo Usuário"); // Ou deixe como opcional
        }

        ExtraIncomeDto created = service.createExtraIncome(dto);
        URI location = URI.create("/api/extra-incomes/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    // 2. Listar TODAS as rendas extras do usuário
    @Operation(summary = "Lista todas as Rendas Extras do usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Lista de Rendas Extras retornada com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ExtraIncomeDto.class))
            ))
    @ApiResponse(responseCode = "404", description = "Nenhuma Renda Extra encontrada para o usuário (se o serviço retornar nulo ou lançar exceção)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))) // Supondo que ErrorResponseDto exista
    @GetMapping
    public ResponseEntity<List<ExtraIncomeDto>> listAllExtraIncomes() {
        List<ExtraIncomeDto> list = service.findAllByCurrentUser();
        // O serviço já retorna Collections.emptyList() se nada for encontrado,
        // então não precisamos de uma verificação explícita de nulo/vazio aqui para retornar 404.
        // Um 200 OK com lista vazia é uma resposta válida para "listar todos".
        return ResponseEntity.ok(list);
    }


    // 3.1 Listar rendas extras por banco
    @Operation(summary = "Lista todas as Rendas Extras do usuário por Banco")
    @ApiResponse(responseCode = "200", description = "Lista de Rendas Extras retornada com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ExtraIncomeDto.class))
            ))
    @ApiResponse(responseCode = "404", description = "Nenhuma Renda Extra encontrada para o banco informado ou banco não pertence ao usuário",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @GetMapping("/bank/{bankId}")
    public ResponseEntity<List<ExtraIncomeDto>> listByBank(
            @Parameter(description = "UUID do Banco", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID bankId
    ) {
        List<ExtraIncomeDto> list = service.listByBank(bankId);
        return ResponseEntity.ok(list); // O serviço lançará ResourceNotFoundException se o banco não for encontrado/pertencer ao usuário
    }

    // 3.2 Listar rendas extras por categoria
    @Operation(summary = "Lista todas as Rendas Extras do usuário por Categoria")
    @ApiResponse(responseCode = "200", description = "Lista de Rendas Extras retornada com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ExtraIncomeDto.class))
            ))
    @ApiResponse(responseCode = "404", description = "Nenhuma Renda Extra encontrada para a categoria informada",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ExtraIncomeDto>> listByCategory(
            @Parameter(description = "UUID da Categoria", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID categoryId
    ) {
        List<ExtraIncomeDto> list = service.listByCategory(categoryId);
        return ResponseEntity.ok(list);
    }

    // 4. Buscar UMA renda extra específica
    @Operation(summary = "Busca uma única Renda Extra por ID")
    @ApiResponse(responseCode = "200", description = "Renda Extra retornada com sucesso",
            content = @Content(schema = @Schema(implementation = ExtraIncomeDto.class)))
    @ApiResponse(responseCode = "404", description = "Renda Extra não encontrada para o usuário",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @GetMapping("/{id}")
    public ResponseEntity<ExtraIncomeDto> getOne(
            @Parameter(description = "UUID da Renda Extra", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id
    ) {
        ExtraIncomeDto dto = service.getById(id);
        return ResponseEntity.ok(dto);
    }

    // 5. Atualizar UMA renda extra específica
    @Operation(summary = "Atualiza uma Renda Extra por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Renda Extra atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = ExtraIncomeDto.class))),
            @ApiResponse(responseCode = "400", description = "Campo obrigatório enviado vazio ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Renda Extra ou recursos associados (ex: Banco) não encontrados para o usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ExtraIncomeDto> updateOne(
            @Parameter(description = "UUID da Renda Extra a ser atualizada", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para atualização da Renda Extra (campos opcionais: name, description, amount, date, categoryId, bankId)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ExtraIncomeUpdateDto.class))
            )
            @Valid @RequestBody ExtraIncomeUpdateDto dto
    ) {
        // Validações de campos vazios podem ser feitas no serviço ou com anotações no DTO.
        // Ex: if (dto.getName() != null && dto.getName().trim().isEmpty()) { throw new IllegalArgumentException("O campo name não pode ser vazio se fornecido."); }
        ExtraIncomeDto updated = service.updateExtraIncome(id, dto);
        return ResponseEntity.ok(updated);
    }

    // 6. Atualizar TODAS as rendas extras do usuário de uma vez
    @Operation(summary = "Atualiza todas as Rendas Extras do usuário (lote)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rendas Extras atualizadas com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ExtraIncomeDto.class))
                    )),
            @ApiResponse(responseCode = "400", description = "Campo obrigatório enviado vazio ou inválido em algum item",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Nenhuma Renda Extra cadastrada para o usuário ou ID não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping
    public ResponseEntity<List<ExtraIncomeDto>> batchUpdate(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Lista de objetos com campos obrigatórios: id e quaisquer outros campos a atualizar",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ExtraIncomeBatchUpdateDto.class)))
            )
            @Valid @RequestBody List<ExtraIncomeBatchUpdateDto> dtos
    ) {
        List<ExtraIncomeDto> updatedList = service.batchUpdate(dtos);
        return ResponseEntity.ok(updatedList);
    }

    // 7.1 Deletar UMA renda extra específica
    @Operation(summary = "Deleta uma única Renda Extra por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Renda Extra deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Renda Extra não encontrada para o usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOne(
            @Parameter(description = "UUID da Renda Extra a ser deletada", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id
    ) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // 7.2 Deletar TODAS as rendas extras do usuário
    @Operation(summary = "Deleta todas as Rendas Extras cadastradas pelo usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Todas as Rendas Extras foram deletadas com sucesso (ou nenhuma existia)"),
            // O serviço não lança mais 404 se não houver nada para deletar, então remover essa resposta.
            // @ApiResponse(responseCode = "404", description = "O usuário não possui nenhuma Renda Extra cadastrada",
            //         content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        service.deleteAll();
        return ResponseEntity.noContent().build();
    }

    // 7.3 Deletar TODAS as rendas extras associadas a um banco específico
    @Operation(summary = "Deleta todas as Rendas Extras de um Banco específico do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rendas Extras desse banco deletadas com sucesso (ou nenhuma existia)"),
            @ApiResponse(responseCode = "404", description = "Banco não encontrado ou não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/bank/{bankId}")
    public ResponseEntity<Void> deleteAllByBank(
            @Parameter(description = "UUID do Banco", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID bankId
    ) {
        service.deleteAllByBank(bankId);
        return ResponseEntity.noContent().build();
    }

    // 7.4 Deletar TODAS as rendas extras associadas a uma categoria específica
    @Operation(summary = "Deleta todas as Rendas Extras de uma Categoria específica do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rendas Extras dessa categoria deletadas com sucesso (ou nenhuma existia)"),
            // Se a categoria não existir, o serviço pode não lançar um 404, mas apenas não deletar nada.
            // @ApiResponse(responseCode = "404", description = "Nenhuma Renda Extra encontrada para a categoria informada",
            //         content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/category/{categoryId}")
    public ResponseEntity<Void> deleteAllByCategory(
            @Parameter(description = "UUID da Categoria", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID categoryId
    ) {
        service.deleteAllByCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    // 8.1 Somar valor a UMA renda extra específica
    @Operation(summary = "Adiciona um valor ao campo amount de uma Renda Extra específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Valor somado com sucesso",
                    content = @Content(schema = @Schema(implementation = ExtraIncomeAmountOperationDto.class))),
            @ApiResponse(responseCode = "400", description = "Campo 'value' ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Renda Extra não encontrada para o usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PatchMapping("/{id}/add")
    public ResponseEntity<ExtraIncomeAmountOperationDto> addToOne(
            @Parameter(description = "UUID da Renda Extra", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "JSON contendo o campo 'value' com o valor a ser somado (ex: { \"value\": 25.50 })",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ValueDto.class)) // Supondo que ValueDto exista
            )
            @Valid @RequestBody ValueDto body
    ) {
        ExtraIncomeAmountOperationDto result = service.addToOne(id, body.getValue());
        return ResponseEntity.ok(result);
    }

    // 8.2 Somar valor a TODAS as rendas extras do usuário
    @Operation(summary = "Adiciona um valor ao campo amount de todas as Rendas Extras do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Valores somados com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ExtraIncomeAmountOperationDto.class)))),
            @ApiResponse(responseCode = "400", description = "Campo 'value' ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não possui nenhuma Renda Extra cadastrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PatchMapping("/add")
    public ResponseEntity<List<ExtraIncomeAmountOperationDto>> addToAll(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "JSON contendo o campo 'value' com o valor a ser somado (ex: { \"value\": 10.00 })",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ValueDto.class))
            )
            @Valid @RequestBody ValueDto body
    ) {
        List<ExtraIncomeAmountOperationDto> resultList = service.addToAll(body.getValue());
        return ResponseEntity.ok(resultList);
    }

    // 8.3 Subtrair valor de UMA renda extra específica
    @Operation(summary = "Subtrai um valor do campo amount de uma Renda Extra específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Valor subtraído com sucesso",
                    content = @Content(schema = @Schema(implementation = ExtraIncomeAmountOperationDto.class))),
            @ApiResponse(responseCode = "400", description = "Campo 'value' ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Renda Extra não encontrada para o usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PatchMapping("/{id}/subtract")
    public ResponseEntity<ExtraIncomeAmountOperationDto> subtractFromOne(
            @Parameter(description = "UUID da Renda Extra", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "JSON contendo o campo 'value' com o valor a ser subtraído (ex: { \"value\": 5.00 })",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ValueDto.class))
            )
            @Valid @RequestBody ValueDto body
    ) {
        ExtraIncomeAmountOperationDto result = service.subtractFromOne(id, body.getValue());
        return ResponseEntity.ok(result);
    }

    // 8.4 Subtrair valor de TODAS as rendas extras do usuário
    @Operation(summary = "Subtrai um valor do campo amount de todas as Rendas Extras do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Valores subtraídos com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ExtraIncomeAmountOperationDto.class)))),
            @ApiResponse(responseCode = "400", description = "Campo 'value' ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não possui nenhuma Renda Extra cadastrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PatchMapping("/subtract")
    public ResponseEntity<List<ExtraIncomeAmountOperationDto>> subtractFromAll(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "JSON contendo o campo 'value' com o valor a ser subtraído (ex: { \"value\": 2.50 })",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ValueDto.class))
            )
            @Valid @RequestBody ValueDto body
    ) {
        List<ExtraIncomeAmountOperationDto> resultList = service.subtractFromAll(body.getValue());
        return ResponseEntity.ok(resultList);
    }

    // 9. Transferência entre duas rendas extras
    @Operation(summary = "Transfere valor entre duas Rendas Extras")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transferência realizada com sucesso",
                    content = @Content(schema = @Schema(implementation = ExtraIncomeTransferDto.class))),
            @ApiResponse(responseCode = "400", description = "Campo obrigatório ausente (fromId, toId ou value) ou IDs iguais",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Alguma das Rendas Extras (fromId ou toId) não encontrada para o usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/transfer")
    public ResponseEntity<ExtraIncomeTransferDto> transfer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "JSON com campos fromId, toId e value (ex: { \"fromId\": \"uuidOrigem\", \"toId\": \"uuidDestino\", \"value\": 20.00 })",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ExtraIncomeTransferRequestDto.class)) // Supondo que ExtraIncomeTransferRequestDto exista
            )
            @Valid @RequestBody ExtraIncomeTransferRequestDto dto
    ) {
        ExtraIncomeTransferDto result = service.transfer(dto.getFromId(), dto.getToId(), dto.getValue());
        return ResponseEntity.ok(result);
    }
}
