// ExtraIncomeCreateDto.java
package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ExtraIncomeCreateDto", description = "Dados para criar uma nova Renda Extra")
public class ExtraIncomeCreateDto {

    @NotNull
    @Schema(description = "Nome da renda extra", example = "Freelance de design", required = true)
    private String name;

    @Schema(description = "Descrição opcional da renda extra", example = "Pagamento de projeto de logo")
    private String description;

    @NotNull
    @Schema(description = "Valor da renda extra", example = "250.75", required = true)
    private BigDecimal amount;

    @NotNull
    @Schema(description = "Data da renda extra (YYYY-MM-DD)", example = "2025-06-01", required = true)
    private LocalDate date;

    @NotNull
    @Schema(description = "UUID da categoria associada", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
    private UUID categoryId;

    @NotNull
    @Schema(description = "UUID do banco associado", example = "7b4e65a2-1234-4b56-89cd-0a1b2c3d4e5f", required = true)
    private UUID bankId;
}
