package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal; // Importar BigDecimal

@Data
@NoArgsConstructor
@Schema(name = "BankCreateDto", description = "Dados para criar um novo banco")
public class BankCreateDto {

    @NotBlank
    @Schema(description = "Nome do banco", example = "Banco Principal", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Descrição opcional do banco", example = "Conta corrente principal para despesas")
    private String description;

    // Novo campo opcional para o saldo inicial
    @Schema(description = "Saldo inicial opcional do banco (pode ser positivo ou negativo). Se não informado, será 0.", example = "1500.50")
    private BigDecimal initialBalance; // Nome do campo no DTO
}