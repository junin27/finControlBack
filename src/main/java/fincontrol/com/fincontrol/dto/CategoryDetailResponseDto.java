package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CategoryDetailResponseDto", description = "Resposta detalhada para operações de categoria, incluindo dados do usuário e da categoria.")
public class CategoryDetailResponseDto {

    @Schema(description = "Dados do usuário proprietário")
    private UserSimpleDto user;

    @Schema(description = "Dados da categoria")
    private CategoryDataDto category;
}
