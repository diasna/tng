package uk.diasna.tng.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * Generates 16-character alphanumeric tracking numbers (A-Z, 0-9)
 * Uses timestamp + random data for uniqueness
 */
@Component
public class TrackingNumberGenerator {

    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Generate a 16-character tracking number
     * Format: [8 chars timestamp-based][8 chars random]
     */
    public String generateTrackingNumber() {
        String timestampPart = encodeTimestamp(Instant.now().toEpochMilli());
        String randomPart = generateRandomString(8);
        return timestampPart + randomPart;
    }
    
    /**
     * Encode timestamp into 8-character base-36 string (A-Z, 0-9)
     */
    private String encodeTimestamp(long timestamp) {
        StringBuilder result = new StringBuilder();
        
        // Use only the lower 40 bits of timestamp to avoid overflow
        long value = timestamp & 0xFFFFFFFFFFL;
        
        for (int i = 0; i < 8; i++) {
            result.append(CHARSET.charAt((int)(value % CHARSET.length())));
            value /= CHARSET.length();
        }
        
        return result.toString();
    }
    
    /**
     * Generate random string of specified length using A-Z, 0-9
     */
    private String generateRandomString(int length) {
        StringBuilder result = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            result.append(CHARSET.charAt(secureRandom.nextInt(CHARSET.length())));
        }
        
        return result.toString();
    }
}
