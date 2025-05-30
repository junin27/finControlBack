package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate; // Import para LocalDate
import java.util.UUID;

@Data
@Schema(name="ExpenseCreateDto", description="Dados para criar uma nova despesa")
public class ExpenseCreateDto {
    @NotNull
    @Schema(description="Nome da despesa", example="Conta de luz", required=true)
    private String name;

    @Schema(description="Descrição opcional", example="Pagamento mensal")
    private String description;

    @NotNull
    @Schema(description="Valor da despesa", example="150.75", required=true)
    private BigDecimal value;

    @NotNull
    @Schema(description="ID da categoria", example="b9244a85-9d51-46e7-b626-259259862ad1", required=true)
    private UUID categoryId;

    @Schema(description="ID do banco (opcional)", example="daa0e2a7-2ad6-42b9-8271-1d7e9facc027")
    private UUID bankId;

    @Schema(description="Data em que a despesa ocorreu (opcional, formato YYYY-MM-DD)", example="2025-05-27")
    private LocalDate expenseDate; // Novo campo opcional
}