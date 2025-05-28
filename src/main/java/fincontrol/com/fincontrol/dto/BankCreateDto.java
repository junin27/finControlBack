package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema; // Adicionar import
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor; // Adicionado para consistência, opcional

@Data
@NoArgsConstructor // Adicionado para consistência, opcional
@Schema(name = "BankCreateDto", description = "Dados para criar um novo banco")
public class BankCreateDto {

    @NotBlank
    @Schema(description = "Nome do banco", example = "Banco Inter", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Descrição opcional do banco", example = "Conta digital para recebimentos")
    private String description;
}