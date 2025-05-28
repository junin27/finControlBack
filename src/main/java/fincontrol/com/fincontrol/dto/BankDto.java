package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema; // Adicionar import
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; // Adicionado para consistência

import java.math.BigDecimal;
import java.time.LocalDateTime; // Import não usado neste DTO, mas mantido se você planeja adicionar campos de timestamp
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor // Adicionado para consistência
@Schema(name = "BankDto", description = "Dados detalhados de um banco, incluindo totais de receitas e despesas")
public class BankDto {

    @Schema(description = "UUID do banco", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private UUID id;

    @Schema(description = "Nome do banco", example = "Banco Principal")
    private String name;

    @Schema(description = "Descrição do banco", example = "Conta corrente para despesas do dia a dia")
    private String description;

    @Schema(description = "Soma total das receitas associadas a este banco", example = "7500.00")
    private BigDecimal totalIncome;

    @Schema(description = "Soma total das despesas associadas a este banco", example = "3250.50")
    private BigDecimal totalExpense;

}