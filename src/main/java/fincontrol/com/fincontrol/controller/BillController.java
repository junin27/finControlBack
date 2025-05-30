package fincontrol.com.fincontrol.controller;

import fincontrol.com.fincontrol.dto.BillCreateDto;
import fincontrol.com.fincontrol.dto.BillResponseDto;
import fincontrol.com.fincontrol.dto.BillUpdateDto;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException; // If you use it
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.model.enums.BillStatus;
import fincontrol.com.fincontrol.repository.UserRepository;
import fincontrol.com.fincontrol.service.BillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
@RequestMapping("/api/bills") // Changed from "/api/contas-pagar"
@Tag(name = "Bills", description = "Management of user's accounts payable (bills)") // Changed tag name
public class BillController {

    private final BillService billService;
    private final UserRepository userRepository;

    public BillController(BillService billService, UserRepository userRepository) {
        this.billService = billService;
        this.userRepository = userRepository;
    }

    private UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            // This should ideally be handled by Spring Security returning 401
            throw new ResourceNotFoundException("User not authenticated.");
        }
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found with email: " + userEmail));
        return user.getId();
    }

    @Operation(summary = "Create a new bill for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bill created successfully", content = @Content(schema = @Schema(implementation = BillResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data provided (e.g., past due date, insufficient bank balance for auto-pay setup if linked)"),
            @ApiResponse(responseCode = "404", description = "User, Expense, or Bank not found")
    })
    @PostMapping
    public ResponseEntity<BillResponseDto> createBill(@Valid @RequestBody BillCreateDto createDto) {
        UUID userId = getAuthenticatedUserId();
        BillResponseDto responseDto = billService.createBill(createDto, userId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(responseDto.getId()).toUri();
        return ResponseEntity.created(location).body(responseDto);
    }

    @Operation(summary = "Get a specific bill by ID for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bill found", content = @Content(schema = @Schema(implementation = BillResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Bill not found or does not belong to the user")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BillResponseDto> getBillById(
            @Parameter(description = "ID of the bill to retrieve") @PathVariable UUID id) {
        UUID userId = getAuthenticatedUserId();
        BillResponseDto responseDto = billService.getBillByIdAndUser(id, userId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "List bills for the authenticated user with optional filters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of bills returned successfully")
    })
    @GetMapping
    public ResponseEntity<List<BillResponseDto>> getAllBills(
            @Parameter(description = "Filter by bill status", in = ParameterIn.QUERY, name = "status", schema = @Schema(implementation = BillStatus.class))
            @RequestParam(required = false) BillStatus status,
            @Parameter(description = "Filter by expense category ID", in = ParameterIn.QUERY, name = "expenseCategoryId")
            @RequestParam(required = false) UUID expenseCategoryId,
            @Parameter(description = "Filter by associated bank ID", in = ParameterIn.QUERY, name = "bankId")
            @RequestParam(required = false) UUID bankId
    ) {
        UUID userId = getAuthenticatedUserId();
        List<BillResponseDto> responseDtos = billService.getAllBillsFiltered(userId, status, expenseCategoryId, bankId);
        return ResponseEntity.ok(responseDtos);
    }

    @Operation(summary = "Update an existing bill for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bill updated successfully", content = @Content(schema = @Schema(implementation = BillResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data or operation not allowed (e.g., updating a paid bill)"),
            @ApiResponse(responseCode = "404", description = "Bill, Expense, or Bank not found, or bill does not belong to user")
    })
    @PatchMapping("/{id}") // Using PATCH for partial updates
    public ResponseEntity<BillResponseDto> updateBill(
            @Parameter(description = "ID of the bill to update") @PathVariable UUID id,
            @Valid @RequestBody BillUpdateDto updateDto) {
        UUID userId = getAuthenticatedUserId();
        BillResponseDto responseDto = billService.updateBill(id, updateDto, userId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Delete a bill for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Bill deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Bill not found or does not belong to the user")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBill(
            @Parameter(description = "ID of the bill to delete") @PathVariable UUID id) {
        UUID userId = getAuthenticatedUserId();
        billService.deleteBill(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Manually mark a bill as paid for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bill marked as paid", content = @Content(schema = @Schema(implementation = BillResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid operation (e.g., bill already paid)"),
            @ApiResponse(responseCode = "404", description = "Bill not found or does not belong to the user")
    })
    @PatchMapping("/{id}/pay") // Changed from "/pagar"
    public ResponseEntity<BillResponseDto> markBillAsPaidManually(
            @Parameter(description = "ID of the bill to mark as paid") @PathVariable UUID id) {
        UUID userId = getAuthenticatedUserId();
        BillResponseDto responseDto = billService.markAsPaidManually(id, userId);
        return ResponseEntity.ok(responseDto);
    }
}
