package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Schema(name = "ExtraIncomeDto", description = "Dados para criação de uma entrada de renda extra")
public class ExtraIncomeDto {

    @NotNull
    @Positive
    @Schema(description = "Valor da renda extra (em reais)", example = "350.00", required = true)
    private BigDecimal amount;

    @NotNull
    @Schema(description = "UUID da categoria associada à renda extra", example = "e7a1c3b2-4f9d-4a3d-8c9e-1b2d3f4a5e6f", required = true)
    private UUID categoryId;

    @Size(max = 255)
    @Schema(description = "Descrição opcional da renda extra", example = "Freelance de design")
    private String description;

    @NotNull
    @Schema(description = "Data da entrada no formato YYYY-MM-DD", example = "2025-05-18", required = true)
    private LocalDate date;
}
