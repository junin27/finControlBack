package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "CategoryCreateDto", description = "Dados para criar uma nova categoria")
public class CategoryCreateDto {

    @NotBlank(message = "O campo name é obrigatório, pois não é possível criar uma categoria sem nome.")
    @Size(min = 1, max = 100, message = "O nome da categoria deve ter entre 1 e 100 caracteres.")
    @Schema(description = "Nome da categoria.", example = "Alimentação", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 255, message = "A descrição não pode exceder 255 caracteres.")
    @Schema(description = "Descrição detalhada opcional da categoria.", example = "Gastos com supermercado e restaurantes")
    private String description; // Opcional, @PrePersist na entidade cuidará do default
}
    