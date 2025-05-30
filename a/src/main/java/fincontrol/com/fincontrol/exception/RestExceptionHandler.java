package fincontrol.com.fincontrol.config; // Seu pacote pode ser diferente, ex: fincontrol.com.fincontrol.exception

import fincontrol.com.fincontrol.exception.InsufficientBalanceException; // Importar
import fincontrol.com.fincontrol.exception.InvalidOperationException;   // Importar
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest; // Para obter o path
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException; // Para erros de validação @Valid
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime; // Para timestamp no DTO de erro mais completo
import java.util.HashMap; // Para construir o Map de forma mais flexível
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centraliza o tratamento de exceções da API.
 */
@RestControllerAdvice
public class RestExceptionHandler {

    // Seu handler existente
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // Novo handler para InsufficientBalanceException
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientBalance(InsufficientBalanceException ex, HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value()); // 400 Bad Request é comum para este tipo de erro
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Novo handler para InvalidOperationException
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOperation(InvalidOperationException ex, HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value()); // Ou outro status apropriado, como 422 Unprocessable Entity
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Handler para erros de validação do @Valid nos DTOs (MethodArgumentNotValidException)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Erro de Validação");
        body.put("path", request.getRequestURI());

        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        body.put("messages", details); // Lista de mensagens de erro dos campos

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Handler genérico para outras exceções RuntimeException não tratadas (opcional, mas útil)
    // Coloque este por último, pois os mais específicos são verificados primeiro.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleGenericRuntimeException(RuntimeException ex, HttpServletRequest request) {
        // É uma boa prática logar a exceção completa no servidor para debugging
        // log.error("Erro inesperado na requisição {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        body.put("message", "Ocorreu um erro inesperado no servidor. Por favor, tente novamente mais tarde.");
        body.put("path", request.getRequestURI());

        // Não exponha detalhes da exceção interna para o cliente por padrão por segurança,
        // a menos que seja uma exceção com uma mensagem já preparada e segura para o cliente.
        // Se você tiver uma forma de identificar exceções "seguras", poderia usar ex.getMessage().

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}