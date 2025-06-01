// ExtraIncomeAmountOperationDto.java
package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ExtraIncomeAmountOperationDto", description = "DTO para operações de soma/subtração em Renda Extra")
public class ExtraIncomeAmountOperationDto {

    @Schema(description = "UUID da renda extra afetada", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Valor anterior antes da operação", example = "100.00")
    private BigDecimal previousAmount;

    @Schema(description = "Valor adicionado na operação (somente para soma)", example = "25.50")
    private BigDecimal addedValue;

    @Schema(description = "Valor subtraído na operação (somente para subtração)", example = "5.00")
    private BigDecimal subtractedValue;

    @Schema(description = "Novo valor após a operação", example = "125.50")
    private BigDecimal newAmount;
}
