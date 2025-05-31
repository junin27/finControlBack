package fincontrol.com.fincontrol.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ErrorResponse", description = "Estrutura padronizada para respostas de erro da API")
public class ErrorResponseDto {

    @Schema(description = "Timestamp da ocorrência do erro", example = "2025-05-31T04:00:00Z")
    private LocalDateTime timestamp;

    @Schema(description = "Código de status HTTP", example = "400")
    private int status;

    @Schema(description = "Frase curta do status HTTP, indicando o tipo de erro", example = "Bad Request")
    private String error;

    @Schema(description = "Mensagem principal descrevendo o erro ou um resumo dos erros de validação.", example = "Um ou mais campos falharam na validação. Veja os detalhes.")
    private String message;

    @Schema(description = "Caminho da requisição que originou o erro", example = "/api/categories")
    private String path;

    @Schema(description = "Lista de mensagens de erro detalhadas, especialmente para falhas de validação de múltiplos campos. Cada string geralmente contém o nome do campo e a mensagem de erro específica.", example = "[\"name: O campo name é obrigatório, pois não é possível criar uma categoria sem nome.\"]", nullable = true)
    private List<String> details;

    // Construtor para erros com uma única mensagem principal (sem lista de 'details')
    public ErrorResponseDto(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
