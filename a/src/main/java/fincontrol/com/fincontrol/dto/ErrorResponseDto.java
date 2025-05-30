package fincontrol.com.fincontrol.dto.error; // Crie um subpacote 'error' em 'dto'

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDto {
    private LocalDateTime timestamp;
    private int status;
    private String error; // Ex: "Bad Request", "Not Found"
    private String message;
    private String path;
    private List<String> details; // Para múltiplos erros de validação, por exemplo

    public ErrorResponseDto(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}