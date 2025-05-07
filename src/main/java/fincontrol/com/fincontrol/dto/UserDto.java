package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@Schema(name = "UserDto", description = "Dados de um usuário retornados pela API")
public class UserDto {

    @Schema(description = "Identificador único do usuário", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Nome completo do usuário", example = "Fulano da Silva")
    private String name;

    @Schema(description = "E-mail de login", example = "fulano@example.com")
    private String email;

    @Schema(description = "Salário do usuário", example = "3000.00")
    private BigDecimal salary;
}
