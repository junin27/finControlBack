package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CategoryData", description = "Detalhes completos da categoria")
public class CategoryDataDto {

    @Schema(description = "UUID da categoria", example = "7fa85f64-1234-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Nome da categoria", example = "Alimentação")
    private String name;

    @Schema(description = "Descrição detalhada da categoria", example = "Gastos com supermercado e restaurantes")
    private String description;

    @Schema(description = "Timestamp de criação", example = "2025-05-18T13:45:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp da última atualização", example = "2025-05-18T14:00:00")
    private LocalDateTime updatedAt;
}