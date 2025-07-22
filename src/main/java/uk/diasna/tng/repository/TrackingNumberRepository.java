package uk.diasna.tng.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.diasna.tng.entity.TrackingNumberEntity;

import java.util.Optional;

@Repository
public interface TrackingNumberRepository extends JpaRepository<TrackingNumberEntity, Long> {
    /**
     * Check if a tracking number already exists
     */
    boolean existsByTrackingNumber(String trackingNumber);
    
    /**
     * Find tracking number by the tracking number string
     */
    Optional<TrackingNumberEntity> findByTrackingNumber(String trackingNumber);
    
    /**
     * Count tracking numbers for a specific customer
     */
    @Query("SELECT COUNT(t) FROM TrackingNumberEntity t WHERE t.customerId = :customerId")
    long countByCustomerId(@Param("customerId") java.util.UUID customerId);
}
