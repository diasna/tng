package uk.diasna.tng.exception;

public class TrackingNumberGenerationException extends RuntimeException {
    
    public TrackingNumberGenerationException(String message) {
        super(message);
    }
    
    public TrackingNumberGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
