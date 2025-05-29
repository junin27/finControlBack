package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Schema(name = "VaultCreateDto", description = "Dados para criar um novo cofre")
public class VaultCreateDto {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Nome do cofre", example = "Economias para Viagem", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 255)
    @Schema(description = "Descrição opcional do cofre", example = "Juntar dinheiro para viagem de férias")
    private String description;

    @NotNull
    @PositiveOrZero // Não aceitar valores negativos
    @Schema(description = "Valor inicial a ser guardado no cofre", example = "500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal initialAmount;

    @NotBlank
    @Size(max = 10)
    @Schema(description = "Moeda do cofre (ex: BRL, USD)", example = "BRL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String currency;

    @Schema(description = "ID do banco de onde o valor inicial será retirado (opcional)", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private UUID bankId;
}