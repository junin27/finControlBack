package fincontrol.com.fincontrol.dto;

import fincontrol.com.fincontrol.model.enums.ReceiptMethodEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(name = "ReceivableUpdateDto", description = "Data for updating an existing receivable account. Only provided fields will be updated.")
public class ReceivableUpdateDto {

    @Schema(description = "New method of receipt (optional)", example = "BANK_SLIP")
    private ReceiptMethodEnum receiptMethod;

    @Schema(description = "New expected date of receipt (optional, YYYY-MM-DD). Cannot be in the past.", example = "2026-01-15")
    private LocalDate dueDate;

    @Schema(description = "Change automatic bank receipt flag (optional)", example = "false")
    private Boolean automaticBankReceipt;


}