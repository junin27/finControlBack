package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(name = "VaultTransactionRequestDto", description = "Dados para realizar uma transação (saque/depósito) em um cofre")
public class VaultTransactionRequestDto {

    @NotNull
    @Positive(message = "O valor da transação deve ser positivo.")
    @Schema(description = "Valor a ser sacado ou depositado no cofre", example = "50.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;
}