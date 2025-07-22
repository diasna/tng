package uk.diasna.tng.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TrackingNumberRequest(
    String originCountryId,
    String destinationCountryId,
    BigDecimal weight,
    UUID customerId,
    String customerName,
    String customerSlug
) {}
