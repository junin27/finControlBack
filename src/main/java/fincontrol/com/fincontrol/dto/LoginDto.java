package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "LoginDto", description = "Credenciais para autenticação")
public class LoginDto {

    @Schema(description = "E-mail de login", example = "fulano@example.com", required = true)
    private String email;

    @Schema(description = "Senha de acesso", example = "senhaSuperSecreta", required = true)
    private String password;
}
