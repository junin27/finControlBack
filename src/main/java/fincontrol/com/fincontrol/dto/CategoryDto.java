package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank; // Import para @NotBlank
import jakarta.validation.constraints.Size;    // Import para @Size
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

    @Schema(description = "UUID da categoria", example = "7fa85f64-1234-4562-b3fc-2c963f66afa6", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "UUID do usuário dono da categoria", example = "2ec7d1c2-a306-4ffe-9603-dc39408d5241", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID userId;

    @NotBlank(message = "O campo name é obrigatório, pois não é possível criar uma categoria sem nome.")
    @Size(min = 1, max = 100, message = "O nome da categoria deve ter entre 1 e 100 caracteres.")
    @Schema(description = "Nome da categoria (obrigatório na criação).", example = "Alimentação")
    private String name; // Este campo representa o nome da categoria

    @Schema(description = "Descrição detalhada da categoria (opcional). Se não informado na criação, assume valor padrão.", example = "Gastos com supermercado e restaurantes")
    private String description; // Este é o novo campo opcional de descrição

    @Schema(description = "Timestamp de criação", example = "2025-05-18T13:45:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp da última atualização", example = "2025-05-18T14:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}
