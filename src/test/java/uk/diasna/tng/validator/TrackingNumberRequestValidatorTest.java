package uk.diasna.tng.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.diasna.tng.dto.TrackingNumberRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TrackingNumberRequestValidatorTest {

    private TrackingNumberRequestValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TrackingNumberRequestValidator();
    }

    @Test
    void validate_ValidRequest_NoException() {
        // Given
        TrackingNumberRequest request = createValidRequest();

        // When & Then
        assertDoesNotThrow(() -> validator.validate(request));
        assertTrue(validator.isValid(request));
        assertTrue(validator.collectValidationErrors(request).isEmpty());
    }

    @Test
    void validate_InvalidOriginCountryId_ThrowsException() {
        // Given
        TrackingNumberRequest request = new TrackingNumberRequest(
            "INVALID", "ID", new BigDecimal("1.234"),
            UUID.randomUUID(), "Test Customer", "test-customer"
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> validator.validate(request)
        );
        
        assertTrue(exception.getMessage().contains("Origin country ID must be ISO 3166-1 alpha-2 format"));
    }

    @Test
    void validate_NullWeight_ThrowsException() {
        // Given
        TrackingNumberRequest request = new TrackingNumberRequest(
            "MY", "ID", null,
            UUID.randomUUID(), "Test Customer", "test-customer"
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> validator.validate(request)
        );
        
        assertTrue(exception.getMessage().contains("Weight is required"));
    }

    @Test
    void validate_WeightTooLow_ThrowsException() {
        // Given
        TrackingNumberRequest request = new TrackingNumberRequest(
            "MY", "ID", new BigDecimal("0.0005"),
            UUID.randomUUID(), "Test Customer", "test-customer"
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> validator.validate(request)
        );
        
        assertTrue(exception.getMessage().contains("Weight must be at least"));
    }

    @Test
    void validate_WeightTooHigh_ThrowsException() {
        // Given
        TrackingNumberRequest request = new TrackingNumberRequest(
            "MY", "ID", new BigDecimal("1000000.0"),
            UUID.randomUUID(), "Test Customer", "test-customer"
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> validator.validate(request)
        );
        
        assertTrue(exception.getMessage().contains("Weight must not exceed"));
    }

    @Test
    void validate_InvalidCustomerSlug_ThrowsException() {
        // Given
        TrackingNumberRequest request = new TrackingNumberRequest(
            "MY", "ID", new BigDecimal("1.234"),
            UUID.randomUUID(), "Test Customer", "Invalid_Slug!"
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> validator.validate(request)
        );
        
        assertTrue(exception.getMessage().contains("Customer slug must be in kebab-case format"));
    }

    @Test
    void validate_MultipleErrors_CombinesAllErrors() {
        // Given
        TrackingNumberRequest request = new TrackingNumberRequest(
            null, "INVALID", new BigDecimal("0"),
            null, "", "Invalid_Slug!"
        );

        // When
        List<String> errors = validator.collectValidationErrors(request);

        // Then
        assertEquals(6, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Origin country ID is required")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("Destination country ID must be ISO 3166-1 alpha-2 format")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("Weight must be at least")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("Customer ID is required")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("Customer name is required")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("Customer slug must be in kebab-case format")));
    }

    @Test
    void isValid_ValidRequest_ReturnsTrue() {
        // Given
        TrackingNumberRequest request = createValidRequest();

        // When & Then
        assertTrue(validator.isValid(request));
    }

    @Test
    void isValid_InvalidRequest_ReturnsFalse() {
        // Given
        TrackingNumberRequest request = new TrackingNumberRequest(
            null, null, null, null, null, null
        );

        // When & Then
        assertFalse(validator.isValid(request));
    }

    private TrackingNumberRequest createValidRequest() {
        return new TrackingNumberRequest(
            "MY", "ID", new BigDecimal("1.234"),
            UUID.randomUUID(), "Test Customer", "test-customer"
        );
    }
}
