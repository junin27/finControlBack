// ExtraIncomeTransferRequestDto.java
package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ExtraIncomeTransferRequestDto", description = "DTO de requisição para transferência entre duas Rendas Extra")
public class ExtraIncomeTransferRequestDto {

    @NotNull
    @Schema(description = "UUID da Renda Extra de origem", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
    private UUID fromId;

    @NotNull
    @Schema(description = "UUID da Renda Extra de destino", example = "7b4e65a2-1234-4b56-89cd-0a1b2c3d4e5f", required = true)
    private UUID toId;

    @NotNull
    @Schema(description = "Valor a ser transferido", example = "20.00", required = true)
    private BigDecimal value;
}
