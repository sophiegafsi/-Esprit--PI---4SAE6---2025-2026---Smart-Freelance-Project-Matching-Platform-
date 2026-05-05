package freelink.condidature.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<java.util.Map<String, String>> handleException(Exception e) {
        System.err.println("### EXCEPTION CAUGHT IN GLOBAL HANDLER ###");
        e.printStackTrace();
        
        java.util.Map<String, String> errorResponse = new java.util.HashMap<>();
        errorResponse.put("message", "Server Error: " + e.getMessage());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
