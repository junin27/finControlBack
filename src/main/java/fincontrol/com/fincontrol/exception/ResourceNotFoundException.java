package fincontrol.com.fincontrol.exception;

// Não precisa de @ResponseStatus aqui se for tratado pelo @ControllerAdvice
public class
ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}