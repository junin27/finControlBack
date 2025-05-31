package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "CategoryMassUpdateDto", description = "Dados para atualizar todas as categorias de um usuário com os mesmos valores. Envie apenas os campos que deseja atualizar em todas as categorias.")
public class CategoryMassUpdateDto {

    @Size(min = 1, max = 100, message = "O nome da categoria, se fornecido, deve ter entre 1 e 100 caracteres.")
    @Schema(description = "Novo nome para TODAS as categorias do usuário (opcional). Se fornecido, não pode ser vazio.", example = "Categoria Padronizada")
    private String name;

    @Size(max = 255, message = "A descrição não pode exceder 255 caracteres.")
    @Schema(description = "Nova descrição para TODAS as categorias do usuário (opcional). Envie uma string vazia para limpar.", example = "Descrição Padrão para Todas")
    private String description;
}