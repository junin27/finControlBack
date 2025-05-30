package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(name = "UserRegisterDto", description = "Dados para registro de um novo usuário")
public class UserRegisterDto {

    @NotBlank(message = "O nome completo é obrigatório.")
    @Pattern(regexp = "^[a-zA-ZÀ-ú']{2,}(\\s[a-zA-ZÀ-ú']{2,})+$", message = "Você precisa informar o seu nome completo com ao menos duas palavras, e cada palavra deve ter no mínimo 2 letras (somente letras e apóstrofos são permitidos).")
    @Size(min = 5, max = 100, message = "O nome completo deve ter entre 5 e 100 caracteres.")
    @Schema(description = "Nome completo do usuário (mínimo duas palavras, cada com pelo menos 2 letras). Exemplo: João Silva", example = "João Silva", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Informe um email válido, exemplo seuemail@email.com")
    @Size(max = 150, message = "O e-mail não pode exceder 150 caracteres.")
    @Schema(description = "E-mail de login", example = "joao.silva@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 6, max = 100, message = "A sua senha deve ter no mínimo 6 e no máximo 100 caracteres.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{6,}$", message = "A sua senha está fraca, ela precisa possuir no mínimo 6 caracteres, com ao menos uma letra e um número.")
    @Schema(description = "Senha de acesso (mínimo 6 caracteres, com letras e números)", example = "Senha@123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "A confirmação de senha é obrigatória.")
    @Size(min = 6, max = 100, message = "A confirmação de senha deve ter no mínimo 6 e no máximo 100 caracteres.")
    @Schema(description = "Confirmação da senha de acesso (deve ser igual à senha)", example = "Senha@123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;

    @NotNull(message = "O salário mensal é obrigatório.")
    @PositiveOrZero(message = "Seu salário mensal precisa ser maior ou igual a 0.")
    @Schema(description = "Salário inicial do usuário", example = "3500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal salary;
}
   