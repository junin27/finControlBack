// ExtraIncomeTransferDto.java
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
@Schema(name = "ExtraIncomeTransferDto", description = "DTO de resposta para operação de transferência entre Rendas Extra")
public class ExtraIncomeTransferDto {

    @Schema(description = "UUID da Renda Extra de origem", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID fromId;

    @Schema(description = "UUID da Renda Extra de destino", example = "7b4e65a2-1234-4b56-89cd-0a1b2c3d4e5f")
    private UUID toId;

    @Schema(description = "Valor anterior da Renda Extra de origem", example = "100.00")
    private BigDecimal fromPreviousAmount;

    @Schema(description = "Valor anterior da Renda Extra de destino", example = "50.00")
    private BigDecimal toPreviousAmount;

    @Schema(description = "Valor transferido", example = "20.00")
    private BigDecimal transferredValue;

    @Schema(description = "Novo valor da Renda Extra de origem após transferência", example = "80.00")
    private BigDecimal fromNewAmount;

    @Schema(description = "Novo valor da Renda Extra de destino após transferência", example = "70.00")
    private BigDecimal toNewAmount;

    @Schema(description = "Timestamp da transferência (ISO 8601)", example = "2025-06-01T14:30:00Z")
    private String timestamp;
}
