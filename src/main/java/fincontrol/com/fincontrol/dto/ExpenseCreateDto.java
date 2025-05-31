package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank; // Adicionado para name
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size; // Adicionado para name
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Schema(name="ExpenseCreateDto", description="Dados para criar uma nova despesa")
public class ExpenseCreateDto {

    @NotBlank(message = "O campo name é obrigatório, pois não é possível criar uma despesa sem nome.")
    @Size(min = 1, max = 100, message = "O nome da despesa deve ter entre 1 e 100 caracteres.")
    @Schema(description="Nome da despesa", example="Conta de luz", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description="Descrição opcional", example="Pagamento mensal referente a maio")
    private String description;

    @NotNull(message = "O campo value é obrigatório, pois não é possível criar uma despesa sem valor.")
    @DecimalMin(value = "0.01", message = "O campo value necessita ser maior que 0.") // Garante que é positivo e não zero
    @Schema(description="Valor da despesa (deve ser maior que 0)", example="150.75", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal value;

    @NotNull(message = "O campo categoryId é obrigatório, pois não é possível criar uma despesa sem categoria.")
    @Schema(description="ID da categoria associada", example="b9244a85-9d51-46e7-b626-259259862ad1", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID categoryId;

    @Schema(description="ID do banco para associar à despesa (opcional)", example="daa0e2a7-2ad6-42b9-8271-1d7e9facc027")
    private UUID bankId;

    @NotNull(message = "O campo expenseDate é obrigatório, pois não é possível criar uma despesa sem data.")
    @Schema(description="Data em que a despesa ocorreu (formato YYYY-MM-DD)", example="2025-05-27", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate expenseDate;
}
