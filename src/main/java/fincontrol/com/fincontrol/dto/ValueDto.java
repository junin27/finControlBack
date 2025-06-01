
package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ValueDto", description = "DTO para operações de soma/subtração contendo apenas o campo value")
public class ValueDto {

    @NotNull
    @Schema(description = "Valor a ser usado na operação de soma/subtração", example = "10.00", required = true)
    private BigDecimal value;
}
