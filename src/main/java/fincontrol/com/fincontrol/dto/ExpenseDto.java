package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
// import lombok.Getter; // Getter é coberto por @Data

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@Schema(name = "ExpenseDto", description = "Dados detalhados de uma despesa")
public class ExpenseDto {

    @Schema(description = "UUID da despesa", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Nome da despesa", example = "Conta de Luz")
    private String name;

    @Schema(description = "Descrição detalhada da despesa", example = "Pagamento mensal referente à energia elétrica")
    private String description;

    @Schema(description = "Valor da despesa", example = "150.75")
    private BigDecimal value;

    @Schema(description = "UUID da categoria da despesa", example = "b9244a85-9d51-46e7-b626-259259862ad1")
    private UUID categoryId;

    @Schema(description = "Descrição da categoria da despesa", example = "Moradia")
    private String categoryDescription;

    @Schema(description = "UUID do banco associado à despesa (opcional)", example = "daa0e2a7-2ad6-42b9-8271-1d7e9facc027")
    private UUID bankId; // Pode ser nulo se a despesa não estiver associada a um banco

    @Schema(description = "Timestamp de criação da despesa", example = "2025-05-28T10:15:30")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp da última atualização da despesa", example = "2025-05-28T10:20:00")
    private LocalDateTime updatedAt;
}