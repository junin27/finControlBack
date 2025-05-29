package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Removida a importação de jakarta.validation.constraints.NotBlank se não for mais usada aqui
// A validação de 'name' obrigatório na criação será feita no controller.

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

    @Schema(description = "Nome da categoria (obrigatório na criação)", example = "Alimentação")
    private String name; // Antigo 'description'

    @Schema(description = "Descrição detalhada da categoria (opcional). Se não informado na criação, assume valor padrão.", example = "Gastos com supermercado e restaurantes")
    private String description; // Novo campo 'description'

    @Schema(description = "Timestamp de criação", example = "2025-05-18T13:45:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp de atualização", example = "2025-05-18T14:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}