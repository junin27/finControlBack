package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime; // Para o timestamp

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BankTransferResponseDto", description = "Resposta detalhada da operação de transferência entre bancos")
public class BankTransferResponseDto {

    @Schema(description = "Mensagem de status da operação")
    private String message;

    @Schema(description = "Valor da transferência realizada")
    private BigDecimal transferAmount;

    @Schema(description = "Detalhes do banco de origem")
    private BankTransferLegDto sourceBankInfo;

    @Schema(description = "Detalhes do banco de destino")
    private BankTransferLegDto destinationBankInfo;

    @Schema(description = "Timestamp da transação de transferência")
    private LocalDateTime timestamp;
}