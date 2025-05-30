package fincontrol.com.fincontrol.dto;

import fincontrol.com.fincontrol.model.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Schema(name = "BillCreateDto", description = "Data for creating a new bill")
public class BillCreateDto {

    @NotNull(message = "Expense ID is mandatory.")
    @Schema(description = "ID of the associated expense", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID expenseId;

    @Schema(description = "Bank ID for payment (optional)")
    private UUID bankId;

    @NotNull(message = "Payment method is mandatory.")
    @Schema(description = "Payment method", example = "BANK_SLIP", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"CASH", "CREDIT_CARD", "DEBIT_CARD", "PIX", "BANK_SLIP", "CHECK", "LOAN", "TRANSFER", "CRYPTOCURRENCY", "OTHER"})
    private PaymentMethod paymentMethod;

    @NotNull(message = "Due date is mandatory.")
    @FutureOrPresent(message = "Due date cannot be in the past.")
    @Schema(description = "Bill due date (YYYY-MM-DD)", example = "2025-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate dueDate;

    @NotNull(message = "Auto-pay flag is mandatory.")
    @Schema(description = "Indicates if payment should be attempted automatically on due date", example = "false", defaultValue = "false")
    private Boolean autoPay = false;
}
    