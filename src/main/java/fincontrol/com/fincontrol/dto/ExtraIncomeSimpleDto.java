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
@Schema(name = "ExtraIncomeSimpleDto", description = "Simplified extra income data for receivable context")
public class ExtraIncomeSimpleDto {

    @Schema(description = "Extra Income ID", example = "39fa4c61-dfc5-4191-8571-f18073dc2e88")
    private UUID id;

    @Schema(description = "Description of the extra income", example = "Freelance Project X")
    private String name;

    @Schema(description = "Value of the extra income", example = "1250.75")
    private BigDecimal value; // Mapeie de amount

    @Schema(description = "ID of the bank associated with this extra income (if any)", example = "f1e2d3c4-b5a6-7890-1234-abcdef567890", nullable = true)
    private UUID bankId;

    @Schema(description = "Name of the bank associated with this extra income (if any)", example = "Main Bank Account", nullable = true)
    private String bankName;
}
