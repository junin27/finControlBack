package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
// import lombok.AllArgsConstructor; // Removido para usar o construtor explícito

import java.math.BigDecimal;
import java.time.LocalDate; // Import para LocalDate
import java.time.LocalDateTime;
import java.util.UUID;

@Data
// @AllArgsConstructor // Removido pois temos um construtor explícito agora
@Schema(name = "ExpenseDto", description = "Dados detalhados de uma despesa")
public class ExpenseDto {

    @Schema(description = "UUID da despesa", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Nome da despesa", example = "Conta de Luz")
    private String name;

    @Schema(description = "Descrição detalhada da despesa", example = "Pagamento mensal referente à energia elétrica")
    private String description;

    @Schema(description = "Valor da despesa", example = "150.75")
    private BigDecimal value;

    @Schema(description = "Data em que a despesa ocorreu (formato YYYY-MM-DD)", example = "2025-05-27", nullable = true)
    private LocalDate expenseDate; // Novo campo

    @Schema(description = "UUID da categoria da despesa", example = "b9244a85-9d51-46e7-b626-259259862ad1", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID categoryId;

    @Schema(description = "Nome da categoria da despesa", example = "Moradia", accessMode = Schema.AccessMode.READ_ONLY)
    private String categoryName;

    @Schema(description = "UUID do banco associado à despesa (pode ser nulo)", example = "daa0e2a7-2ad6-42b9-8271-1d7e9facc027", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID bankId;

    @Schema(description = "Nome/Status do banco associado", example = "Banco Principal / Banco não Informado", accessMode = Schema.AccessMode.READ_ONLY)
    private String bankDisplayName;

    @Schema(description = "Timestamp de criação da despesa", example = "2025-05-28T10:15:30", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp da última atualização da despesa", example = "2025-05-28T10:20:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    // Construtor explícito para incluir o novo campo
    public ExpenseDto(UUID id, String name, String description, BigDecimal value, LocalDate expenseDate,
                      UUID categoryId, String categoryName, UUID bankId, String bankDisplayName,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.value = value;
        this.expenseDate = expenseDate; // Atribui o novo campo
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.bankId = bankId;
        this.bankDisplayName = bankDisplayName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Adicione um construtor sem argumentos se necessário (ex: para desserialização do Jackson se @RequestBody usar este DTO diretamente,
    // embora para criação/update você tenha DTOs específicos)
    public ExpenseDto() {}
}