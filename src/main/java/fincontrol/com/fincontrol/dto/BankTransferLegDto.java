package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BankTransferLegDto", description = "Detalhes de um banco envolvido na transferência, incluindo saldos antes e depois")
public class BankTransferLegDto {

    @Schema(description = "ID do banco")
    private UUID bankId;

    @Schema(description = "Nome do banco")
    private String bankName;

    @Schema(description = "Saldo do banco ANTES da transferência")
    private BigDecimal balanceBeforeTransfer;

    @Schema(description = "Saldo do banco APÓS da transferência")
    private BigDecimal balanceAfterTransfer;
}