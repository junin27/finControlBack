package fincontrol.com.fincontrol.dto;

import fincontrol.com.fincontrol.model.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Schema(name = "BillUpdateDto", description = "Data for updating an existing bill")
public class BillUpdateDto {

    @Schema(description = "New ID of the associated expense (optional)")
    private UUID expenseId;

    @Schema(description = "New Bank ID for payment (optional, send null to disassociate)")
    private UUID bankId;

    @Schema(description = "New payment method (optional)", example = "PIX",
            allowableValues = {"CASH", "CREDIT_CARD", "DEBIT_CARD", "PIX", "BANK_SLIP", "CHECK", "LOAN", "TRANSFER", "CRYPTOCURRENCY", "OTHER"})
    private PaymentMethod paymentMethod;

    @FutureOrPresent(message = "Due date cannot be in the past.")
    @Schema(description = "New bill due date (optional, YYYY-MM-DD)", example = "2026-01-15")
    private LocalDate dueDate;

    @Schema(description = "Change auto-pay flag (optional)", example = "true")
    private Boolean autoPay;
}
    