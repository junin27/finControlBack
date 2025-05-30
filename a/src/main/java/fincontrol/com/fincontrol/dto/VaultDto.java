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
@Schema(name = "VaultDto", description = "Dados detalhados de um cofre")
public class VaultDto {

    @Schema(description = "ID do cofre", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Nome do cofre")
    private String name;

    @Schema(description = "Descrição do cofre")
    private String description;

    @Schema(description = "Valor atual no cofre", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal amount;

    @Schema(description = "Moeda do cofre")
    private String currency;

    @Schema(description = "ID do banco ao qual o cofre pode estar vinculado (opcional)", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID bankId;

    @Schema(description = "Nome do banco ao qual o cofre pode estar vinculado (opcional)", accessMode = Schema.AccessMode.READ_ONLY)
    private String bankName;

    @Schema(description = "ID do usuário proprietário do cofre", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID userId;

    @Schema(description = "Data e hora de criação do cofre", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Data e hora da última atualização do cofre", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}