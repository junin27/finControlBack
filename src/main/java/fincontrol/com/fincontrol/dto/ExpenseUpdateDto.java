package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate; // Import para LocalDate
import java.util.UUID;

@Data
@Schema(name="ExpenseUpdateDto", description="Dados para atualizar uma despesa existente")
public class ExpenseUpdateDto {
    @Schema(description="Nome da despesa", example="Conta de água")
    private String name;

    @Schema(description="Descrição opcional", example="Pagamento mensal revisado")
    private String description;

    @Schema(description="Valor da despesa", example="120.00")
    private BigDecimal value;

    @Schema(description="ID da categoria", example="b9244a85-9d51-46e7-b626-259259862ad1")
    private UUID categoryId;

    @Schema(description="ID do banco (opcional)", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID bankId;

    @Schema(description="Nova data em que a despesa ocorreu (opcional, formato YYYY-MM-DD). Envie null para remover a data.", example="2025-05-28")
    private LocalDate expenseDate; // Novo campo opcional
}