package es.uco.pw.pw2526.api;

import java.time.format.DateTimeParseException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "es.uco.pw.pw2526.api")
public class RestErrorHandler {

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<String> handleDateTimeParseException(DateTimeParseException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Formato de fecha inválido.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException exception) {
        String mensaje = exception.getMessage();
        if (mensaje == null || mensaje.isBlank()) {
            mensaje = "Petición inválida.";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mensaje);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor.");
    }
}