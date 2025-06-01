package fincontrol.com.fincontrol.controller;

import fincontrol.com.fincontrol.dto.*;

import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.Expense;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.repository.UserRepository;
import fincontrol.com.fincontrol.service.ExpenseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expenses")
@Tag(name = "Despesas", description = "CRUD de despesas do usuário")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserRepository userRepository;

    public ExpenseController(ExpenseService expenseService, UserRepository userRepository) {
        this.expenseService = expenseService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUserEntity() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId;

        try {
            userId = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Identificador do usuário autenticado inválido: " + userIdStr);
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado com id: " + userIdStr));
    }


    @Operation(summary = "Cria uma nova despesa para o usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Despesa criada com sucesso",
                    content = @Content(schema = @Schema(implementation = ExpenseDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuário, Categoria ou Banco não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping
    public ResponseEntity<ExpenseDetailResponseDto> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para criar uma nova despesa",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ExpenseCreateDto.class))
            )
            @Valid @RequestBody ExpenseCreateDto dto) {
        User authenticatedUser = getAuthenticatedUserEntity();
        Expense createdExpenseEntity = expenseService.create(dto, authenticatedUser.getId());
        ExpenseDetailResponseDto responseDto = toExpenseDetailResponseDto(createdExpenseEntity, authenticatedUser);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdExpenseEntity.getId())
                .toUri();

        return ResponseEntity.created(location).body(responseDto);
    }

    @Operation(summary = "Lista todas as despesas do usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Lista de despesas retornada com sucesso",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ExpenseDetailResponseDto.class))))
    @GetMapping
    public List<ExpenseDetailResponseDto> listAll() {
        User authenticatedUser = getAuthenticatedUserEntity();
        return expenseService.listAllByAuthenticatedUser(authenticatedUser.getId())
                .stream()
                .map(expenseEntity -> toExpenseDetailResponseDto(expenseEntity, authenticatedUser))
                .collect(Collectors.toList());
    }

    @Operation(summary = "Busca uma despesa específica por ID para o usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Despesa encontrada",
                    content = @Content(schema = @Schema(implementation = ExpenseDetailResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Despesa não encontrada ou não pertence ao usuário",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDetailResponseDto> getById(@Parameter(description = "ID da despesa") @PathVariable UUID id) {
        User authenticatedUser = getAuthenticatedUserEntity();
        Expense expenseEntity = expenseService.findByIdAndUserIdEnsureOwnership(id, authenticatedUser.getId());
        return ResponseEntity.ok(toExpenseDetailResponseDto(expenseEntity, authenticatedUser));
    }

    @Operation(summary = "Atualiza uma despesa existente do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Despesa atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = ExpenseDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Despesa não encontrada ou não pertence ao usuário",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDetailResponseDto> update(
            @Parameter(description = "ID da despesa a ser atualizada") @PathVariable UUID id,
            @Valid @RequestBody ExpenseUpdateDto dto) {
        User authenticatedUser = getAuthenticatedUserEntity();
        Expense updatedExpenseEntity = expenseService.update(id, dto, authenticatedUser.getId());
        return ResponseEntity.ok(toExpenseDetailResponseDto(updatedExpenseEntity, authenticatedUser));
    }

    @Operation(summary = "Deleta uma despesa existente do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Despesa deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Despesa não encontrada ou não pertence ao usuário",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da despesa a ser deletada") @PathVariable UUID id) {
        User authenticatedUser = getAuthenticatedUserEntity();
        expenseService.delete(id, authenticatedUser.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualiza campos específicos em TODAS as despesas do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Despesas atualizadas com sucesso",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ExpenseDetailResponseDto.class)))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping("/user-all")
    public ResponseEntity<List<ExpenseDetailResponseDto>> massUpdateUserExpenses(
            @Valid @RequestBody ExpenseMassUpdateDto dto) {
        User authenticatedUser = getAuthenticatedUserEntity();
        List<Expense> updatedExpenses = expenseService.massUpdateUserExpenses(dto, authenticatedUser.getId());
        List<ExpenseDetailResponseDto> responseDtos = updatedExpenses.stream()
                .map(expenseEntity -> toExpenseDetailResponseDto(expenseEntity, authenticatedUser))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @Operation(summary = "Deleta TODAS as despesas do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Todas as despesas foram deletadas com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/user-all")
    public ResponseEntity<Void> deleteAllUserExpenses() {
        User authenticatedUser = getAuthenticatedUserEntity();
        expenseService.deleteAllUserExpenses(authenticatedUser.getId());
        return ResponseEntity.noContent().build();
    }

    private ExpenseDetailResponseDto toExpenseDetailResponseDto(Expense expense, User user) {
        UserSimpleDto userSimpleDto = new UserSimpleDto(user.getId(), user.getName());

        CategorySimpleDto categorySimpleDto = null;
        if (expense.getCategory() != null) {
            categorySimpleDto = new CategorySimpleDto(
                    expense.getCategory().getId(),
                    expense.getCategory().getName()
            );
        }

        BankSimpleDto bankSimpleDto = null;
        if (expense.getBank() != null) {
            bankSimpleDto = new BankSimpleDto(
                    expense.getBank().getId(),
                    expense.getBank().getName()
            );
        }

        ExpenseDataDto expenseDataDto = new ExpenseDataDto(
                expense.getId(),
                expense.getName(),
                expense.getDescription(),
                expense.getValue(),
                expense.getExpenseDate(),
                categorySimpleDto,
                bankSimpleDto,
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
        return new ExpenseDetailResponseDto(userSimpleDto, expenseDataDto);
    }
}