package uk.diasna.tng.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tracking_numbers", indexes = {
    @Index(name = "idx_tracking_number", columnList = "trackingNumber", unique = true),
    @Index(name = "idx_customer_id", columnList = "customerId"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
public class TrackingNumberEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tracking_number", unique = true, nullable = false, length = 16)
    private String trackingNumber;
    
    @Column(name = "origin_country_id", nullable = false, length = 2)
    private String originCountryId;
    
    @Column(name = "destination_country_id", nullable = false, length = 2)
    private String destinationCountryId;
    
    @Column(name = "weight", nullable = false, precision = 10, scale = 3)
    private BigDecimal weight;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(name = "customer_name", nullable = false)
    private String customerName;
    
    @Column(name = "customer_slug", nullable = false)
    private String customerSlug;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    
    public TrackingNumberEntity() {}
    
    public TrackingNumberEntity(String trackingNumber, String originCountryId, 
                               String destinationCountryId, BigDecimal weight, 
                               UUID customerId, String customerName, String customerSlug) {
        this.trackingNumber = trackingNumber;
        this.originCountryId = originCountryId;
        this.destinationCountryId = destinationCountryId;
        this.weight = weight;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerSlug = customerSlug;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTrackingNumber() {
        return trackingNumber;
    }
    
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
    
    public String getOriginCountryId() {
        return originCountryId;
    }
    
    public void setOriginCountryId(String originCountryId) {
        this.originCountryId = originCountryId;
    }
    
    public String getDestinationCountryId() {
        return destinationCountryId;
    }
    
    public void setDestinationCountryId(String destinationCountryId) {
        this.destinationCountryId = destinationCountryId;
    }
    
    public BigDecimal getWeight() {
        return weight;
    }
    
    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }
    
    public UUID getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getCustomerSlug() {
        return customerSlug;
    }
    
    public void setCustomerSlug(String customerSlug) {
        this.customerSlug = customerSlug;
    }
    
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
