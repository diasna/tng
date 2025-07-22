package uk.diasna.tng.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import uk.diasna.tng.dto.TrackingNumberRequest;
import uk.diasna.tng.dto.TrackingNumberResponse;
import uk.diasna.tng.entity.TrackingNumberEntity;
import uk.diasna.tng.exception.TrackingNumberGenerationException;
import uk.diasna.tng.repository.TrackingNumberRepository;
import uk.diasna.tng.util.TrackingNumberGenerator;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingNumberServiceTest {

    @Mock
    private TrackingNumberRepository repository;

    @Mock
    private TrackingNumberGenerator trackingNumberGenerator;

    private MeterRegistry meterRegistry;
    private TrackingNumberService service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new TrackingNumberService(repository, trackingNumberGenerator, meterRegistry);
    }

    @Test
    void generateTrackingNumber_Success() {
        // Given
        TrackingNumberRequest request = new TrackingNumberRequest(
            "MY", "ID", new BigDecimal("1.234"), 
            UUID.randomUUID(), "Test Customer", "test-customer"
        );
        
        String expectedTrackingNumber = "ABC123DEF456GHI7";
        
        when(trackingNumberGenerator.generateTrackingNumber()).thenReturn(expectedTrackingNumber);
        when(repository.existsByTrackingNumber(expectedTrackingNumber)).thenReturn(false);
        when(repository.save(any(TrackingNumberEntity.class))).thenReturn(new TrackingNumberEntity());

        // When
        TrackingNumberResponse response = service.generateTrackingNumber(request);

        // Then
        assertNotNull(response);
        assertEquals(expectedTrackingNumber, response.trackingNumber());
        assertNotNull(response.createdAt());
        
        verify(repository).existsByTrackingNumber(expectedTrackingNumber);
        verify(repository).save(any(TrackingNumberEntity.class));
    }

    @Test
    void generateTrackingNumber_CollisionDetected() {
        // Given
        TrackingNumberRequest request = new TrackingNumberRequest(
            "MY", "ID", new BigDecimal("1.234"), 
            UUID.randomUUID(), "Test Customer", "test-customer"
        );
        
        String firstNumber = "ABC123DEF456GHI7";
        String secondNumber = "XYZ789UVW012RST3";
        
        when(trackingNumberGenerator.generateTrackingNumber())
            .thenReturn(firstNumber)
            .thenReturn(secondNumber);
        when(repository.existsByTrackingNumber(firstNumber)).thenReturn(true);
        when(repository.existsByTrackingNumber(secondNumber)).thenReturn(false);
        when(repository.save(any(TrackingNumberEntity.class))).thenReturn(new TrackingNumberEntity());

        // When
        TrackingNumberResponse response = service.generateTrackingNumber(request);

        // Then
        assertNotNull(response);
        assertEquals(secondNumber, response.trackingNumber());
        
        verify(repository).existsByTrackingNumber(firstNumber);
        verify(repository).existsByTrackingNumber(secondNumber);
        verify(repository).save(any(TrackingNumberEntity.class));
    }

    @Test
    void generateTrackingNumber_DatabaseConstraintViolation() {
        // Given
        TrackingNumberRequest request = new TrackingNumberRequest(
            "MY", "ID", new BigDecimal("1.234"), 
            UUID.randomUUID(), "Test Customer", "test-customer"
        );
        
        String trackingNumber = "ABC123DEF456GHI7";
        
        when(trackingNumberGenerator.generateTrackingNumber()).thenReturn(trackingNumber);
        when(repository.existsByTrackingNumber(trackingNumber)).thenReturn(false);
        when(repository.save(any(TrackingNumberEntity.class)))
            .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        // When & Then
        assertThrows(TrackingNumberGenerationException.class, 
                    () -> service.generateTrackingNumber(request));
        
        verify(repository).existsByTrackingNumber(trackingNumber);
        verify(repository).save(any(TrackingNumberEntity.class));
    }

    @Test
    void generateTrackingNumber_MaxAttemptsExceeded() {
        // Given
        TrackingNumberRequest request = new TrackingNumberRequest(
            "MY", "ID", new BigDecimal("1.234"), 
            UUID.randomUUID(), "Test Customer", "test-customer"
        );
        
        when(trackingNumberGenerator.generateTrackingNumber()).thenReturn("ABC123DEF456GHI7");
        when(repository.existsByTrackingNumber(anyString())).thenReturn(true);

        // When & Then
        assertThrows(TrackingNumberGenerationException.class, 
                    () -> service.generateTrackingNumber(request));
        
        verify(trackingNumberGenerator, times(10)).generateTrackingNumber();
        verify(repository, times(10)).existsByTrackingNumber(anyString());
        verify(repository, never()).save(any(TrackingNumberEntity.class));
    }

    @Test
    void getStats_ReturnsCorrectStats() {
        // Given - Generate some tracking numbers to create stats
        TrackingNumberRequest request = new TrackingNumberRequest(
            "MY", "ID", new BigDecimal("1.234"), 
            UUID.randomUUID(), "Test Customer", "test-customer"
        );
        
        when(trackingNumberGenerator.generateTrackingNumber()).thenReturn("ABC123DEF456GHI7");
        when(repository.existsByTrackingNumber(anyString())).thenReturn(false);
        when(repository.save(any(TrackingNumberEntity.class))).thenReturn(new TrackingNumberEntity());

        // Generate a tracking number to create stats
        service.generateTrackingNumber(request);

        // When
        TrackingNumberService.TrackingNumberStats stats = service.getStats();

        // Then
        assertNotNull(stats);
        assertEquals(1L, stats.totalGenerated());
        assertEquals(0L, stats.totalCollisions());
        assertEquals(0L, stats.totalFailures());
        assertTrue(stats.avgGenerationTimeMs() >= 0);
    }

    @Test
    void getStats_WithCollisions() {
        // Given
        TrackingNumberRequest request = new TrackingNumberRequest(
            "MY", "ID", new BigDecimal("1.234"), 
            UUID.randomUUID(), "Test Customer", "test-customer"
        );
        
        String firstNumber = "ABC123DEF456GHI7";
        String secondNumber = "XYZ789UVW012RST3";
        
        when(trackingNumberGenerator.generateTrackingNumber())
            .thenReturn(firstNumber)
            .thenReturn(secondNumber);
        when(repository.existsByTrackingNumber(firstNumber)).thenReturn(true);
        when(repository.existsByTrackingNumber(secondNumber)).thenReturn(false);
        when(repository.save(any(TrackingNumberEntity.class))).thenReturn(new TrackingNumberEntity());

        // Generate a tracking number with collision
        service.generateTrackingNumber(request);

        // When
        TrackingNumberService.TrackingNumberStats stats = service.getStats();

        // Then
        assertNotNull(stats);
        assertEquals(1L, stats.totalGenerated());
        assertEquals(1L, stats.totalCollisions()); // One collision detected
        assertEquals(0L, stats.totalFailures());
        assertTrue(stats.avgGenerationTimeMs() >= 0);
    }

    @Test 
    void generateTrackingNumber_UnexpectedException() {
        // Given
        TrackingNumberRequest request = new TrackingNumberRequest(
            "MY", "ID", new BigDecimal("1.234"), 
            UUID.randomUUID(), "Test Customer", "test-customer"
        );
        
        when(trackingNumberGenerator.generateTrackingNumber()).thenReturn("ABC123DEF456GHI7");
        when(repository.existsByTrackingNumber(anyString())).thenReturn(false);
        when(repository.save(any(TrackingNumberEntity.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThrows(TrackingNumberGenerationException.class, 
                    () -> service.generateTrackingNumber(request));
        
        // Verify failure counter is incremented
        TrackingNumberService.TrackingNumberStats stats = service.getStats();
        assertEquals(1L, stats.totalFailures());
    }

    @Test
    void generateTrackingNumber_VerifyEntityCreation() {
        // Given
        UUID customerId = UUID.randomUUID();
        TrackingNumberRequest request = new TrackingNumberRequest(
            "MY", "ID", new BigDecimal("1.234"), 
            customerId, "Test Customer", "test-customer"
        );
        
        String trackingNumber = "ABC123DEF456GHI7";
        
        when(trackingNumberGenerator.generateTrackingNumber()).thenReturn(trackingNumber);
        when(repository.existsByTrackingNumber(trackingNumber)).thenReturn(false);
        when(repository.save(any(TrackingNumberEntity.class))).thenReturn(new TrackingNumberEntity());

        // When
        service.generateTrackingNumber(request);

        // Then
        verify(repository).save(argThat(entity -> 
            entity.getTrackingNumber().equals(trackingNumber) &&
            entity.getOriginCountryId().equals("MY") &&
            entity.getDestinationCountryId().equals("ID") &&
            entity.getWeight().equals(new BigDecimal("1.234")) &&
            entity.getCustomerId().equals(customerId) &&
            entity.getCustomerName().equals("Test Customer") &&
            entity.getCustomerSlug().equals("test-customer") &&
            entity.getCreatedAt() != null
        ));
    }
}
