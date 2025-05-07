package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Schema(name = "CategoryDto", description = "Categoria de transação ou despesa")
public class CategoryDto {

    @Schema(description = "Identificador único da categoria", example = "7fa85f64-1234-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Descrição da categoria", example = "Alimentação")
    private String description;
}
