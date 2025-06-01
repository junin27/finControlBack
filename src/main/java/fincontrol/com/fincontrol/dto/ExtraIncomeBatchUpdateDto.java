// ExtraIncomeBatchUpdateDto.java
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
@Schema(name = "ExtraIncomeBatchUpdateDto", description = "Objeto para atualização em lote de Renda Extra")
public class ExtraIncomeBatchUpdateDto {

    @NotNull
    @Schema(description = "UUID da renda extra a ser atualizada", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
    private UUID id;

    @Schema(description = "Novo nome da renda extra (opcional)", example = "Freelance lote atualizado")
    private String name;

    @Schema(description = "Nova descrição da renda extra (opcional)", example = "Descrição em lote atualizada")
    private String description;

    @Schema(description = "Novo valor da renda extra (opcional)", example = "400.00")
    private BigDecimal amount;

    @Schema(description = "Nova data da renda extra (YYYY-MM-DD) (opcional)", example = "2025-06-10")
    private LocalDate date;

    @Schema(description = "Novo UUID de categoria (opcional)", example = "4d5e6f7a-8b9c-0d1e-2f3a-4b5c6d7e8f9a")
    private UUID categoryId;

    @Schema(description = "Novo UUID de banco (opcional)", example = "9a8b7c6d-5e4f-3a2b-1c0d-9e8f7a6b5c4d")
    private UUID bankId;
}
