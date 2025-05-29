package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal; // Import da sua branch 'feature' para balance

@Data
@NoArgsConstructor // Mantendo a versão mais simples da anotação
@Schema(name = "BankUpdateDto", description = "Dados para atualizar um banco existente")
public class BankUpdateDto {

    @Schema(description = "Novo nome do banco (opcional)", example = "Banco Investimentos")
    private String name;

    @Schema(description = "Nova descrição opcional do banco", example = "Conta para aplicações financeiras")
    private String description;

    // Mantendo o novo campo da sua branch 'feature'
    @Schema(description = "Novo saldo para o banco (opcional). Use com cautela, pois ajusta diretamente o saldo.", example = "2500.75")
    private BigDecimal balance;
}