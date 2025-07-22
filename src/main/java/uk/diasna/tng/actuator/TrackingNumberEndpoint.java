package uk.diasna.tng.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;
import uk.diasna.tng.service.TrackingNumberService;

import java.util.Map;

/**
 * Custom actuator endpoint for tracking number statistics
 * Available at: /actuator/tracking-numbers
 */
@Component
@Endpoint(id = "tracking-numbers")
public class TrackingNumberEndpoint {
    
    private final TrackingNumberService trackingNumberService;
    
    public TrackingNumberEndpoint(TrackingNumberService trackingNumberService) {
        this.trackingNumberService = trackingNumberService;
    }
    
    @ReadOperation
    public Map<String, Object> trackingNumberStats() {
        TrackingNumberService.TrackingNumberStats stats = trackingNumberService.getStats();
        
        return Map.of(
            "service", "Tracking Number Generator",
            "statistics", Map.of(
                "totalGenerated", stats.totalGenerated(),
                "totalCollisions", stats.totalCollisions(),
                "totalFailures", stats.totalFailures(),
                "avgGenerationTimeMs", stats.avgGenerationTimeMs()
            ),
            "performance", Map.of(
                "collisionRate", calculateCollisionRate(stats),
                "failureRate", calculateFailureRate(stats),
                "status", determineStatus(stats)
            )
        );
    }
    
    private double calculateCollisionRate(TrackingNumberService.TrackingNumberStats stats) {
        long total = stats.totalGenerated() + stats.totalCollisions();
        return total > 0 ? (double) stats.totalCollisions() / total * 100 : 0.0;
    }
    
    private double calculateFailureRate(TrackingNumberService.TrackingNumberStats stats) {
        long total = stats.totalGenerated() + stats.totalFailures();
        return total > 0 ? (double) stats.totalFailures() / total * 100 : 0.0;
    }
    
    private String determineStatus(TrackingNumberService.TrackingNumberStats stats) {
        double failureRate = calculateFailureRate(stats);
        double collisionRate = calculateCollisionRate(stats);
        
        if (failureRate > 5.0) return "CRITICAL";
        if (failureRate > 1.0 || collisionRate > 10.0) return "WARNING";
        return "HEALTHY";
    }
}
