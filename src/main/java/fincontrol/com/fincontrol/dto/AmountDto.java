package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Schema(name = "AmountDto", description = "Data transfer object for specifying a monetary amount")
public class AmountDto {

    @NotNull(message = "Amount cannot be null")
    @Schema(description = "The monetary amount", example = "100.50", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;
}