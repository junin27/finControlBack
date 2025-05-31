package fincontrol.com.fincontrol.dto;


import fincontrol.com.fincontrol.model.enums.ReceiptMethodEnum;
import fincontrol.com.fincontrol.model.enums.ReceivableStatusEnum;
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
@Schema(name = "ReceivableResponseDto", description = "Detailed data of a receivable account")
public class ReceivableResponseDto {

    @Schema(description = "Receivable ID", example = "02d1a2b3-c4d5-e6f7-a8b9-c0d1e2f3a4b5", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Associated Extra Income details")
    private ExtraIncomeSimpleDto extraIncome; // DTO simplificado para ExtraIncome

    @Schema(description = "Method of receipt", example = "PIX")
    private ReceiptMethodEnum receiptMethod;

    @Schema(description = "Expected date of receipt", example = "2025-12-31")
    private LocalDate dueDate;

    @Schema(description = "Indicates if receipt is set for automatic processing with the bank", example = "true")
    private Boolean automaticBankReceipt;

    @Schema(description = "Current status of the receivable", example = "PENDING")
    private ReceivableStatusEnum status;

    @Schema(description = "User owning the receivable")
    private UserSimpleDto user; // DTO simplificado para User

    @Schema(description = "Timestamp of when the receivable record was created", example = "2025-06-01T10:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update to the receivable record", example = "2025-06-01T10:05:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}