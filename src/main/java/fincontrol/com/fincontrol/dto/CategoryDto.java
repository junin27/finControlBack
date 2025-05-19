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
@Schema(name = "CategoryDto", description = "Dados de categoria (entrada e saída)")
public class CategoryDto {

    @Schema(description = "UUID da categoria", example = "7fa85f64-1234-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "UUID do usuário dono da categoria", example = "2ec7d1c2-a306-4ffe-9603-dc39408d5241")
    private UUID userId;

    @Schema(description = "Descrição da categoria", example = "Freelance")
    private String description;

    @Schema(description = "Timestamp de criação", example = "2025-05-18T13:45:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp de atualização", example = "2025-05-18T14:00:00")
    private LocalDateTime updatedAt;
}
