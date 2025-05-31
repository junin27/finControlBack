package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "CategoryUpdateDto", description = "Dados para atualizar uma categoria existente. Campos não fornecidos não serão alterados.")
public class CategoryUpdateDto {

    @Size(min = 1, max = 100, message = "O nome da categoria deve ter entre 1 e 100 caracteres.")
    @Schema(description = "Novo nome da categoria (opcional). Se fornecido, não pode ser vazio.", example = "Alimentação Geral")
    private String name; // Não é @NotBlank para permitir atualização parcial (só atualizar description)

    @Size(max = 255, message = "A descrição não pode exceder 255 caracteres.")
    @Schema(description = "Nova descrição detalhada opcional da categoria. Envie uma string vazia para limpar ou não envie para manter a atual.", example = "Todos os gastos com alimentação")
    private String description;
}
    