// ExtraIncomeDto.java
package fincontrol.com.fincontrol.dto;

import fincontrol.com.fincontrol.model.ExtraIncome;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ExtraIncomeDto", description = "DTO de resposta para Renda Extra completa")
public class ExtraIncomeDto {

    @Schema(description = "UUID da renda extra", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Nome da renda extra", example = "Freelance de design")
    private String name;

    @Schema(description = "Descrição da renda extra", example = "Pagamento de projeto de logo")
    private String description;

    @Schema(description = "Valor da renda extra", example = "250.75")
    private BigDecimal amount;

    @Schema(description = "Data da renda extra (YYYY-MM-DD)", example = "2025-06-01")
    private LocalDate date;

    @Schema(description = "UUID da categoria associada", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID categoryId;

    @Schema(description = "UUID do banco associado", example = "7b4e65a2-1234-4b56-89cd-0a1b2c3d4e5f")
    private UUID bankId;

    @Schema(description = "Timestamp de criação (ISO 8601)", example = "2025-06-01T12:00:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp da última atualização (ISO 8601)", example = "2025-06-01T12:05:00Z")
    private Instant updatedAt;

    public static ExtraIncomeDto fromEntity(ExtraIncome ei) {
        return ExtraIncomeDto.builder()
                .id(ei.getId())
                .name(ei.getName())
                .description(ei.getDescription())
                .amount(ei.getAmount())
                .date(ei.getDate())
                .categoryId(ei.getCategoryId())
                // Antes: .bankId(ei.getBankId())
                // Agora:
                .bankId(ei.getBank().getId())
                .createdAt(ei.getCreatedAt())
                .updatedAt(ei.getUpdatedAt())
                .build();
    }

}
