package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserSimpleDto", description = "Dados simplificados do usuário")
public class UserSimpleDto {
    @Schema(description = "ID do usuário")
    private UUID id;
    @Schema(description = "Nome do usuário")
    private String name; // Certifique-se que sua entidade User tem um getter para 'name'
}
    