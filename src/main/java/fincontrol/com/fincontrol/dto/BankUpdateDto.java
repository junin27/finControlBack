package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal; // Importar BigDecimal

@Data
@NoArgsConstructor
@Schema(name = "BankUpdateDto", description = "Dados para atualizar um banco existente")
public class BankUpdateDto {

    @Schema(description = "Novo nome do banco (opcional)", example = "Banco Investimentos")
    private String name;

    @Schema(description = "Nova descrição opcional do banco", example = "Conta para aplicações financeiras")
    private String description;

    // Novo campo opcional para atualizar o saldo diretamente
    @Schema(description = "Novo saldo para o banco (opcional). Use com cautela, pois ajusta diretamente o saldo.", example = "2500.75")
    private BigDecimal balance;
}