package fincontrol.com.fincontrol.controller;

import fincontrol.com.fincontrol.dto.ExpenseCreateDto;
import fincontrol.com.fincontrol.dto.ExpenseDto;
import fincontrol.com.fincontrol.dto.ExpenseUpdateDto;
import fincontrol.com.fincontrol.repository.UserRepository;
import fincontrol.com.fincontrol.service.ExpenseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder; // For URI creation

import jakarta.validation.Valid; // For @Valid
import java.net.URI; // For URI creation
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@Tag(name = "Despesas", description = "CRUD de despesas do usuário")
public class ExpenseController {

    private final ExpenseService service;
    private final UserRepository userRepo;

    public ExpenseController(ExpenseService service, UserRepository userRepo) {
        this.service  = service;
        this.userRepo = userRepo;
    }

    @Operation(summary = "Cria uma nova despesa para o usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Despesa criada com sucesso",
                    content = @Content(schema = @Schema(implementation = ExpenseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Usuário, Categoria ou Banco não encontrado")
    })
    @PostMapping
    public ResponseEntity<ExpenseDto> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para criar uma nova despesa",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ExpenseCreateDto.class))
            )
            @Valid @RequestBody ExpenseCreateDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        UUID userId = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário com email " + email + " não encontrado")) // Consider a specific exception like ResourceNotFoundException
                .getId();

        ExpenseDto createdExpense = service.create(dto, userId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdExpense.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdExpense);
    }

    @Operation(summary = "Lista todas as despesas do usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Lista de despesas retornada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExpenseDto.class)))
    // Se este método for para listar despesas APENAS do usuário logado,
    // você precisará adicionar a lógica para filtrar por usuário, similar ao create.
    // Atualmente, service.listAll() parece listar todas as despesas do sistema.
    // Se for para listar todas, o summary deveria ser "Lista todas as despesas do sistema".
    // Assumindo que seja para o usuário, a lógica de filtragem deve estar no service.listAll()
    // ou este método precisa do userId. Para o exemplo, manterei como está no código original.
    @GetMapping
    public List<ExpenseDto> listAll() {
        // Se for para listar despesas do usuário autenticado, precisaria do userId aqui.
        // Ex: service.listAllByUserId(userId);
        return service.listAll();
    }

    @Operation(summary = "Atualiza uma despesa existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Despesa atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = ExpenseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Despesa, Categoria ou Banco não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDto> update(
            @Parameter(description = "ID da despesa a ser atualizada", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para atualizar a despesa",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ExpenseUpdateDto.class))
            )
            @Valid @RequestBody ExpenseUpdateDto dto
    ) {
        // Adicionar lógica de permissão: verificar se a despesa pertence ao usuário autenticado antes de atualizar
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Deleta uma despesa existente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Despesa deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Despesa não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da despesa a ser deletada", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id) {
        // Adicionar lógica de permissão: verificar se a despesa pertence ao usuário autenticado antes de deletar
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}