package uk.diasna.tng.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.diasna.tng.dto.TrackingNumberRequest;
import uk.diasna.tng.dto.TrackingNumberResponse;
import uk.diasna.tng.entity.TrackingNumberEntity;
import uk.diasna.tng.exception.TrackingNumberGenerationException;
import uk.diasna.tng.repository.TrackingNumberRepository;
import uk.diasna.tng.util.TrackingNumberGenerator;

import java.time.OffsetDateTime;

@Service
public class TrackingNumberService {
    
    private static final Logger logger = LoggerFactory.getLogger(TrackingNumberService.class);
    private static final int MAX_GENERATION_ATTEMPTS = 10;
    
    private final TrackingNumberRepository repository;
    private final TrackingNumberGenerator idGenerator;
    private final Counter generationCounter;
    private final Counter collisionCounter;
    private final Counter failureCounter;
    private final Timer generationTimer;
    
    public TrackingNumberService(TrackingNumberRepository repository, 
                               TrackingNumberGenerator idGenerator,
                               MeterRegistry meterRegistry) {
        this.repository = repository;
        this.idGenerator = idGenerator;
        
        this.generationCounter = Counter.builder("tracking_number.generated")
            .description("Total tracking numbers generated")
            .register(meterRegistry);
            
        this.collisionCounter = Counter.builder("tracking_number.collisions")
            .description("Tracking number collisions detected")
            .register(meterRegistry);
            
        this.failureCounter = Counter.builder("tracking_number.failures")
            .description("Tracking number generation failures")
            .register(meterRegistry);
            
        this.generationTimer = Timer.builder("tracking_number.generation.time")
            .description("Time taken to generate tracking number")
            .register(meterRegistry);
    }
    
    @Transactional
    public TrackingNumberResponse generateTrackingNumber(TrackingNumberRequest request) {
        try {
            return generationTimer.recordCallable(() -> {
                logger.info("Generating tracking number for customer: {} from {} to {}", 
                           request.customerId(), request.originCountryId(), request.destinationCountryId());
                
                String trackingNumber = generateUniqueTrackingNumber();
                OffsetDateTime createdAt = OffsetDateTime.now();
                
                try {
                    TrackingNumberEntity entity = new TrackingNumberEntity(
                        trackingNumber,
                        request.originCountryId(),
                        request.destinationCountryId(),
                        request.weight(),
                        request.customerId(),
                        request.customerName(),
                        request.customerSlug()
                    );
                    
                    entity.setCreatedAt(createdAt);
                    repository.save(entity);
                    
                    generationCounter.increment();
                    
                    logger.info("Successfully generated tracking number: {} for customer: {}", 
                               trackingNumber, request.customerId());
                    
                    return new TrackingNumberResponse(trackingNumber, createdAt);
                    
                } catch (DataIntegrityViolationException e) {
                    collisionCounter.increment();
                    logger.warn("Tracking number collision detected: {}, retrying...", trackingNumber);
                    throw new TrackingNumberGenerationException("Tracking number collision detected", e);
                } catch (Exception e) {
                    failureCounter.increment();
                    logger.error("Failed to save tracking number: {}", trackingNumber, e);
                    throw new TrackingNumberGenerationException("Failed to generate tracking number", e);
                }
            });
        } catch (Exception e) {
            if (e instanceof TrackingNumberGenerationException) {
                throw (TrackingNumberGenerationException) e;
            }
            failureCounter.increment();
            throw new TrackingNumberGenerationException("Failed to generate tracking number", e);
        }
    }
    
    private String generateUniqueTrackingNumber() {
        for (int attempt = 1; attempt <= MAX_GENERATION_ATTEMPTS; attempt++) {
            String trackingNumber = idGenerator.generateTrackingNumber();
            
            // Check for existing tracking number
            if (!repository.existsByTrackingNumber(trackingNumber)) {
                logger.debug("Generated unique tracking number: {} on attempt: {}", 
                           trackingNumber, attempt);
                return trackingNumber;
            }
            
            collisionCounter.increment();
            logger.warn("Tracking number collision detected: {} on attempt: {}", 
                       trackingNumber, attempt);
        }
        
        failureCounter.increment();
        throw new TrackingNumberGenerationException(
            "Failed to generate unique tracking number after " + MAX_GENERATION_ATTEMPTS + " attempts");
    }
    
    public TrackingNumberStats getStats() {
        long totalGenerated = (long) generationCounter.count();
        long totalCollisions = (long) collisionCounter.count();
        long totalFailures = (long) failureCounter.count();
        double avgGenerationTime = generationTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
        
        return new TrackingNumberStats(totalGenerated, totalCollisions, totalFailures, avgGenerationTime);
    }
    
    public record TrackingNumberStats(
        long totalGenerated,
        long totalCollisions, 
        long totalFailures,
        double avgGenerationTimeMs
    ) {}
}
