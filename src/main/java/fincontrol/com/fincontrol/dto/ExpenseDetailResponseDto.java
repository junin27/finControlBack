package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ExpenseDetailResponseDto", description = "Resposta detalhada para operações de despesa, incluindo dados do usuário e da despesa com seus relacionamentos.")
public class ExpenseDetailResponseDto {

    @Schema(description = "Dados do usuário proprietário")
    private UserSimpleDto user;

    @Schema(description = "Dados da despesa")
    private ExpenseDataDto expense;
}
