package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@Schema(name = "BankBulkUpdateItemDto", description = "Data for updating a single bank in a bulk operation")
public class BankBulkUpdateItemDto {

    @NotNull(message = "Bank ID cannot be null")
    @Schema(description = "ID of the bank to update", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;

    @Schema(description = "New name for the bank (optional)", example = "Updated Bank Name")
    private String name;

    @Schema(description = "New description for the bank (optional)", example = "Updated bank description")
    private String description;

    @Schema(description = "New balance for the bank (optional). Use with caution.", example = "3000.00")
    private BigDecimal balance;
}