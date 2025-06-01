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
@Schema(name = "BankBalanceDetailsDto", description = "Details of a bank including its balance")
public class BankBalanceDetailsDto {
    @Schema(description = "Bank ID")
    private UUID bankId;
    @Schema(description = "Bank name")
    private String bankName;
    @Schema(description = "Bank balance")
    private BigDecimal balance;
}