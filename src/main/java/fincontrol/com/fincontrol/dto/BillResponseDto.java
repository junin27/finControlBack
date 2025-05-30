package fincontrol.com.fincontrol.dto;

import fincontrol.com.fincontrol.model.enums.PaymentMethod;
import fincontrol.com.fincontrol.model.enums.BillStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BillResponseDto", description = "Detailed data of an account payable (bill)")
public class BillResponseDto {

    @Schema(description = "Bill ID")
    private UUID id;

    @Schema(description = "User owning the bill")
    private UserSimpleDto user;

    @Schema(description = "Associated expense details")
    private ExpenseSimpleDto expense;

    @Schema(description = "Bank associated with the payment (can be null)")
    private BankSimpleDto bank;

    @Schema(description = "Payment method")
    private PaymentMethod paymentMethod;

    @Schema(description = "Due date")
    private LocalDate dueDate;

    @Schema(description = "Indicates if payment is automatic")
    private boolean autoPay;

    @Schema(description = "Current status of the bill")
    private BillStatus status;

    @Schema(description = "Date the payment was made (can be null)")
    private LocalDate paymentDate;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Record last update timestamp")
    private LocalDateTime updatedAt;
}
    