package fincontrol.com.fincontrol.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Lançada quando um recurso (usuário, categoria, etc.) não é encontrado.
 * O @ResponseStatus faz o Spring devolver 404 automaticamente.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
