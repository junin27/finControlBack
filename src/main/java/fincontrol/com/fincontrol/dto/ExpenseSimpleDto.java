package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ExpenseSimpleDto", description = "Simplified expense data for a bill")
public class ExpenseSimpleDto {
    @Schema(description = "Expense ID")
    private UUID id;
    @Schema(description = "Expense name")
    private String name; // Assuming your Expense entity has a 'name' field
    @Schema(description = "Expense category details")
    private CategorySimpleDto category;
}