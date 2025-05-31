package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor; // Adicionar se não tiver
// import lombok.AllArgsConstructor; // Removido para usar o construtor explícito que já existe

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor // Adicionado se não tiver e for necessário
@Schema(name = "ExpenseDto", description = "Dados detalhados de uma despesa")
public class ExpenseDto {

    @Schema(description = "UUID da despesa", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Nome da despesa")
    private String name;

    @Schema(description = "Descrição detalhada da despesa")
    private String description;

    @Schema(description = "Valor da despesa")
    private BigDecimal value;

    @Schema(description = "Data em que a despesa ocorreu (formato YYYY-MM-DD)", nullable = true)
    private LocalDate expenseDate;

    @Schema(description = "UUID da categoria da despesa", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID categoryId;

    @Schema(description = "Nome da categoria da despesa", accessMode = Schema.AccessMode.READ_ONLY)
    private String categoryName;

    @Schema(description = "UUID do banco associado à despesa (pode ser nulo)", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID bankId;

    @Schema(description = "Nome/Status do banco associado", accessMode = Schema.AccessMode.READ_ONLY)
    private String bankDisplayName;

    @Schema(description = "Timestamp de criação da despesa", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp da última atualização da despesa", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    // Construtor explícito para incluir todos os campos
    public ExpenseDto(UUID id, String name, String description, BigDecimal value, LocalDate expenseDate,
                      UUID categoryId, String categoryName, UUID bankId, String bankDisplayName,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.value = value;
        this.expenseDate = expenseDate;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.bankId = bankId;
        this.bankDisplayName = bankDisplayName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
    