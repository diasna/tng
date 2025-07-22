package uk.diasna.tng.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record TrackingNumberResponse(
    @JsonProperty("tracking_number")
    String trackingNumber,
    
    @JsonProperty("created_at")
    OffsetDateTime createdAt
) {}
