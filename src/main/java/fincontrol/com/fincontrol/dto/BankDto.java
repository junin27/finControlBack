package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime; // Import agora é usado
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "BankDto", description = "Dados detalhados de um banco, incluindo totais de movimentações, saldo atual e timestamps")
public class BankDto {

    @Schema(description = "UUID do banco", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Nome do banco", example = "Banco Principal")
    private String name;

    @Schema(description = "Descrição do banco", example = "Conta corrente para despesas do dia a dia")
    private String description;

    @Schema(description = "Soma total das receitas (entradas) associadas a este banco", example = "7500.00", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal totalIncome;

    @Schema(description = "Soma total das despesas (saídas) associadas a este banco", example = "3250.50", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal totalExpense;

    @Schema(description = "Saldo atual do banco", example = "4249.50", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal currentBalance;

    @Schema(description = "Timestamp de criação do banco", example = "2025-05-28T10:15:30", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp da última atualização do banco", example = "2025-05-28T10:20:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}