package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
// import java.util.UUID; // No longer needed for this ID
import java.lang.Long; // Explicit import for clarity, though usually not needed
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ExtraIncomeSimpleDto", description = "Simplified extra income data for receivable context")
public class ExtraIncomeSimpleDto {
    @Schema(description = "Extra Income ID", example = "101") // Example changed to Long
    private Long id; // << CHANGED FROM UUID TO Long

    @Schema(description = "Description of the extra income", example = "Freelance Project X")
    private String description;

    @Schema(description = "Value of the extra income", example = "1250.75")
    private BigDecimal value; // This should be populated from extraIncome.getAmount()

    @Schema(description = "ID of the bank associated with this extra income (if any)", example = "f1e2d3c4-b5a6-7890-1234-abcdef567890", nullable = true)
    private UUID bankId; // Assuming Bank ID is still UUID, adjust if not

    @Schema(description = "Name of the bank associated with this extra income (if any)", example = "Main Bank Account", nullable = true)
    private String bankName;
}