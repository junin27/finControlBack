package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "VaultUpdateDto", description = "Dados para atualizar um cofre existente")
public class VaultUpdateDto {

    @Size(max = 100)
    @Schema(description = "Novo nome do cofre (opcional)", example = "Minhas Economias para Férias")
    private String name;

    @Size(max = 255)
    @Schema(description = "Nova descrição opcional do cofre (opcional)", example = "Guardar dinheiro para a viagem de fim de ano")
    private String description;

    @Size(max = 10)
    @Schema(description = "Nova moeda do cofre (opcional, ex: BRL, USD)", example = "BRL")
    private String currency;
}