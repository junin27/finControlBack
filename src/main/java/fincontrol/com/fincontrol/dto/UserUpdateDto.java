package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(name = "UserUpdateDto", description = "Dados para atualização de um usuário existente")
public class UserUpdateDto {

    @Schema(description = "Nome completo do usuário", example = "Fulano da Silva")
    private String name;

    @Schema(description = "Nova senha (deixe vazio para não alterar)", example = "novaSenha123")
    private String password;

    @Schema(description = "Novo salário do usuário", example = "4500.75")
    private BigDecimal salary;
}
