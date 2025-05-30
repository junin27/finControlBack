package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(name = "UserRegisterDto", description = "Dados para registro de um novo usuário")
public class UserRegisterDto {

    @Schema(description = "Nome completo do usuário", example = "Fulano da Silva", required = true)
    private String name;

    @Schema(description = "E-mail de login", example = "fulano@example.com", required = true)
    private String email;

    @Schema(description = "Senha de acesso", example = "senhaSuperSecreta", required = true)
    private String password;

    @Schema(description = "Salário inicial do usuário", example = "3000.00", required = true)
    private BigDecimal salary;
}
