package uk.diasna.tng.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(TrackingNumberGenerationException.class)
    public ResponseEntity<Map<String, Object>> handleTrackingNumberGenerationException(
            TrackingNumberGenerationException ex) {
        
        logger.error("Tracking number generation failed", ex);
        
        Map<String, Object> error = Map.of(
            "error", "TRACKING_NUMBER_GENERATION_FAILED",
            "message", ex.getMessage(),
            "timestamp", OffsetDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        
        logger.warn("Invalid request parameter: {}", ex.getMessage());
        
        Map<String, Object> error = Map.of(
            "error", "INVALID_REQUEST_PARAMETER",
            "message", ex.getMessage(),
            "timestamp", OffsetDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        
        logger.warn("Invalid parameter type: {} for parameter: {}", 
                   ex.getValue(), ex.getName());
        
        Map<String, Object> error = Map.of(
            "error", "INVALID_PARAMETER_TYPE",
            "message", String.format("Invalid value '%s' for parameter '%s'", 
                                   ex.getValue(), ex.getName()),
            "timestamp", OffsetDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        
        logger.error("Unexpected error occurred", ex);
        
        Map<String, Object> error = Map.of(
            "error", "INTERNAL_SERVER_ERROR",
            "message", "An unexpected error occurred",
            "timestamp", OffsetDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
