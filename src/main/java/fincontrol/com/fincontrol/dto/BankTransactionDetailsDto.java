package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BankTransactionDetailsDto", description = "Details of a bank transaction (income or expense)")
public class BankTransactionDetailsDto {
    @Schema(description = "Bank ID associated with the transaction")
    private UUID bankId;
    @Schema(description = "Name of the bank associated with the transaction")
    private String bankName;
    @Schema(description = "Amount of the transaction")
    private BigDecimal amount;
    @Schema(description = "Description of the transaction")
    private String description;
}