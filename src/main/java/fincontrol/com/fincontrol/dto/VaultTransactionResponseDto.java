package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "VaultTransactionResponseDto", description = "Resposta de uma transação de saque ou depósito no cofre")
public class VaultTransactionResponseDto {

    @Schema(description = "ID do cofre")
    private UUID vaultId;

    @Schema(description = "Nome do cofre")
    private String vaultName;

    @Schema(description = "Tipo de transação realizada", example = "WITHDRAWAL ou DEPOSIT")
    private String transactionType;

    @Schema(description = "Valor transacionado")
    private BigDecimal amountTransacted;

    @Schema(description = "Saldo do cofre antes da transação")
    private BigDecimal balanceBefore;

    @Schema(description = "Saldo do cofre após a transação")
    private BigDecimal balanceAfter;

    @Schema(description = "Moeda do cofre")
    private String currency;

    @Schema(description = "ID do banco associado (se houver)")
    private UUID bankId;

    @Schema(description = "Nome do banco associado (se houver)")
    private String bankName;

    @Schema(description = "Data e hora da transação")
    private LocalDateTime transactionTimestamp;
}