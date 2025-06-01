package fincontrol.com.fincontrol.dto;

import fincontrol.com.fincontrol.model.enums.ReceiptMethodEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Schema(name = "ReceivableCreateDto", description = "Data for creating a new receivable account")
public class ReceivableCreateDto {

    @NotNull(message = "Associated Extra Income ID is mandatory.")
    @Schema(description = "ID of the associated Extra Income record", example = "39fa4c61-dfc5-4191-8571-f18073dc2e88", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID extraIncomeId;

    @NotNull(message = "Receipt method is mandatory.")
    @Schema(description = "Method of receipt", example = "PIX", requiredMode = Schema.RequiredMode.REQUIRED)
    private ReceiptMethodEnum receiptMethod;

    @NotNull(message = "Due date for receipt is mandatory.")
    @Schema(description = "Expected date of receipt (YYYY-MM-DD)", example = "2025-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate dueDate;

    @NotNull(message = "Automatic bank receipt flag is mandatory.")
    @Schema(description = "Indicates if receipt should be automatically processed with the bank", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean automaticBankReceipt;
}
