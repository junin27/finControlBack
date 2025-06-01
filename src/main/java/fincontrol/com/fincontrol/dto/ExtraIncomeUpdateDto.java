// ExtraIncomeUpdateDto.java
package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ExtraIncomeUpdateDto", description = "Dados parciais para atualização de Renda Extra")
public class ExtraIncomeUpdateDto {

    @Schema(description = "Novo nome da renda extra (se for alterar)", example = "Freelance atualizado")
    private String name;

    @Schema(description = "Nova descrição da renda extra (se for alterar)", example = "Descrição atualizada ou vazia")
    private String description;

    @Schema(description = "Novo valor da renda extra (se for alterar)", example = "300.00")
    private BigDecimal amount;

    @Schema(description = "Nova data da renda extra (YYYY-MM-DD) (se for alterar)", example = "2025-06-05")
    private LocalDate date;

    @Schema(description = "Novo UUID de categoria (se for alterar)", example = "5e6f7a8b-9c0d-1e2f-3a4b-5c6d7e8f9a0b")
    private UUID categoryId;

    @Schema(description = "Novo UUID de banco (se for alterar)", example = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID bankId;
}
