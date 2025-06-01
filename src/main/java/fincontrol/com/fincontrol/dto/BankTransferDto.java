package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data; // Necessário para gerar getters, setters, etc.
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data // Esta anotação do Lombok gera getAmount(), setAmount(), toString(), equals(), hashCode()
@NoArgsConstructor
@Schema(name = "BankTransferDto", description = "Dados para realizar uma transferência entre bancos do usuário")
public class BankTransferDto {

    @NotNull(message = "O ID do banco de origem não pode ser nulo.")
    @Schema(description = "ID do banco de origem", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID sourceBankId;

    @NotNull(message = "O ID do banco de destino não pode ser nulo.")
    @Schema(description = "ID do banco de destino", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID destinationBankId;

    @NotNull(message = "O valor da transferência não pode ser nulo.")
    @Positive(message = "O valor da transferência deve ser maior que zero.")
    @Schema(description = "Valor a ser transferido", example = "100.50", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount; // O campo está aqui, @Data deve gerar getAmount()
}