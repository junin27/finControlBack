package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal; // Import da sua branch 'feature' para initialBalance

@Data
@NoArgsConstructor // Mantendo a versão mais simples da anotação
@Schema(name = "BankCreateDto", description = "Dados para criar um novo banco")
public class BankCreateDto {

    @NotBlank
    // Mantendo o exemplo da sua branch 'feature', pois parece mais alinhado com as últimas alterações
    @Schema(description = "Nome do banco", example = "Banco Principal", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    // Mantendo o exemplo da sua branch 'feature'
    @Schema(description = "Descrição opcional do banco", example = "Conta corrente principal para despesas")
    private String description;

    // Mantendo o novo campo da sua branch 'feature'
    @Schema(description = "Saldo inicial opcional do banco (pode ser positivo ou negativo). Se não informado, será 0.", example = "1500.50")
    private BigDecimal initialBalance;
}