package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email; // Adicionado para melhor validação de e-mail
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size; // Adicionado para consistência
import lombok.Data;

@Data
@Schema(name = "LoginDto", description = "Credenciais para autenticação")
public class LoginDto {

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Formato de e-mail inválido.") // Validação de formato de e-mail
    @Size(max = 150, message = "O e-mail não pode exceder 150 caracteres.")
    @Schema(description = "E-mail de login", example = "joao.silva@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "A senha é obrigatória.")
    @Schema(description = "Senha de acesso", example = "Senha@123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
