package fincontrol.com.fincontrol.controller;

import fincontrol.com.fincontrol.dto.ErrorResponseDto;
import fincontrol.com.fincontrol.dto.ReceivableCreateDto;
import fincontrol.com.fincontrol.dto.ReceivableResponseDto;
import fincontrol.com.fincontrol.dto.ReceivableUpdateDto; // NOVO DTO IMPORTADO
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.model.enums.ReceivableStatusEnum;
import fincontrol.com.fincontrol.repository.UserRepository;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.service.ReceivableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/receivables")
@Tag(name = "Receivables", description = "Management of user's accounts receivable")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ReceivableController {

    private final ReceivableService receivableService;
    private final UserRepository userRepository;

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

    @Operation(
            summary = "Create a new receivable",
            description = "Creates a new receivable account for the authenticated user, linked to an Extra Income record."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Receivable created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReceivableResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data provided",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Associated Extra Income or User not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReceivableResponseDto> createReceivable(
            @RequestBody(description = "Receivable creation data", required = true,
                    content = @Content(schema = @Schema(implementation = ReceivableCreateDto.class)))
            @Valid @org.springframework.web.bind.annotation.RequestBody ReceivableCreateDto createDto) {
        UUID userId = getAuthenticatedUserId();
        ReceivableResponseDto responseDto = receivableService.createReceivable(createDto, userId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(responseDto.getId()).toUri();
        return ResponseEntity.created(location).body(responseDto);
    }

    @Operation(
            summary = "List receivables with optional filters",
            description = "Lists all receivable accounts for the authenticated user. Allows filtering by status and due date range."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of receivables returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<ReceivableResponseDto>> getAllReceivables(
            @Parameter(description = "Filter by receivable status", in = ParameterIn.QUERY, name = "status", schema = @Schema(implementation = ReceivableStatusEnum.class))
            @RequestParam(required = false) ReceivableStatusEnum status,
            @Parameter(description = "Filter by due date from (YYYY-MM-DD)", in = ParameterIn.QUERY, name = "startDate")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Filter by due date to (YYYY-MM-DD)", in = ParameterIn.QUERY, name = "endDate")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 10, sort = "dueDate") Pageable pageable) {
        UUID userId = getAuthenticatedUserId();
        Page<ReceivableResponseDto> responseDtos = receivableService.getAllReceivablesFiltered(userId, status, startDate, endDate, pageable);
        return ResponseEntity.ok(responseDtos);
    }

    @Operation(
            summary = "Get a specific receivable by ID",
            description = "Retrieves the details of a specific receivable by its ID, if it belongs to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receivable found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReceivableResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Receivable not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReceivableResponseDto> getReceivableById(
            @Parameter(description = "ID of the receivable to retrieve", required = true, example = "02d1a2b3-c4d5-e6f7-a8b9-c0d1e2f3a4b5")
            @PathVariable UUID id) {
        UUID userId = getAuthenticatedUserId();
        ReceivableResponseDto responseDto = receivableService.getReceivableByIdAndUser(id, userId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "Update an existing receivable",
            description = "Updates an existing receivable account (e.g., due date, receipt method). Only non-null fields in the request body will be updated. Cannot update if already received."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receivable updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReceivableResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data or operation not allowed (e.g., updating a received receivable, past due date)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission to update this receivable",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Receivable not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReceivableResponseDto> updateReceivable(
            @Parameter(description = "ID of the receivable to update", required = true, example = "02d1a2b3-c4d5-e6f7-a8b9-c0d1e2f3a4b5")
            @PathVariable UUID id,
            @RequestBody(description = "Receivable update data. Only fields to be changed need to be provided.", required = true,
                    content = @Content(schema = @Schema(implementation = ReceivableUpdateDto.class)))
            @Valid @org.springframework.web.bind.annotation.RequestBody ReceivableUpdateDto updateDto) {
        UUID userId = getAuthenticatedUserId();
        ReceivableResponseDto responseDto = receivableService.updateReceivable(id, updateDto, userId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "Manually mark a receivable as received",
            description = "Marks a PENDING or OVERDUE receivable as RECEIVED or RECEIVED_LATE respectively. This is a manual user action."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receivable successfully marked as received/received_late", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReceivableResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Receivable not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping(value = "/{id}/mark-as-received", produces = MediaType.APPLICATION_JSON_VALUE) // Mudado para PUT para consistência com a ação
    public ResponseEntity<ReceivableResponseDto> markAsReceived(
            @Parameter(description = "ID of the receivable to mark as received", required = true, example = "02d1a2b3-c4d5-e6f7-a8b9-c0d1e2f3a4b5")
            @PathVariable UUID id) {
        UUID userId = getAuthenticatedUserId();
        ReceivableResponseDto responseDto = receivableService.markAsReceivedManually(id, userId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "Delete a receivable",
            description = "Deletes a specific receivable by its ID, if it belongs to the authenticated user. Caution: If the receivable was automatically processed and updated a bank balance, this deletion does not automatically revert the bank transaction."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Receivable deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission to delete this receivable",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Receivable not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReceivable(
            @Parameter(description = "ID of the receivable to delete", required = true, example = "02d1a2b3-c4d5-e6f7-a8b9-c0d1e2f3a4b5")
            @PathVariable UUID id) {
        UUID userId = getAuthenticatedUserId();
        receivableService.deleteReceivable(id, userId);
        return ResponseEntity.noContent().build();
    }
}