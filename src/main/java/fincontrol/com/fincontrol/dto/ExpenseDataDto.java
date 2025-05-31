package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ExpenseDataDto", description = "Detalhes completos da despesa")
public class ExpenseDataDto {

    @Schema(description = "UUID da despesa")
    private UUID id;

    @Schema(description = "Nome da despesa")
    private String name;

    @Schema(description = "Descrição detalhada da despesa")
    private String description;

    @Schema(description = "Valor da despesa")
    private BigDecimal value;

    @Schema(description = "Data em que a despesa ocorreu (formato yyyy-MM-dd)", nullable = true)
    private LocalDate expenseDate;

    @Schema(description = "Categoria da despesa")
    private CategorySimpleDto category; // Objeto Categoria aninhado

    @Schema(description = "Banco associado à despesa (pode ser nulo)", nullable = true)
    private BankSimpleDto bank; // Objeto Banco aninhado (opcional)

    @Schema(description = "Timestamp de criação da despesa")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp da última atualização da despesa")
    private LocalDateTime updatedAt;
}
