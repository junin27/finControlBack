package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(name = "ExtraIncomeResponseDto", description = "Dados de uma renda extra")
public class ExtraIncomeDto {

    @Schema(description = "ID da renda extra", example = "39fa4c61-dfc5-4191-8571-f18073dc2e88")
    private UUID id;

    @NotNull
    @Positive
    @Schema(description = "Valor da renda", example = "500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @NotNull
    @Schema(description = "ID da categoria da renda extra", example = "39fa4c61-dfc5-4191-8571-f18073dc2e88", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID categoryId;

    @Schema(description = "Nome da categoria da renda extra", example = "Salário")
    private String categoryName;

    @Size(max = 255)
    @Schema(description = "Descrição da renda extra", example = "Freelance")
    private String name;

    @NotNull
    @Schema(description = "Data da renda extra", example = "2025-05-31", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate date;

    @Schema(description = "ID do banco associado à renda extra", example = "f1e2d3c4-b5a6-7890-1234-abcdef567890")
    private UUID bankId;

    @Schema(description = "Nome do banco associado", example = "Banco Principal")
    private String bankName;

    @Schema(description = "ID do usuário proprietário da renda extra")
    private UUID userId;

    @Schema(description = "Nome/identificador do usuário proprietário")
    private String userName;

    @Schema(description = "Data de criação do registro")
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização do registro")
    private LocalDateTime updatedAt;
}
