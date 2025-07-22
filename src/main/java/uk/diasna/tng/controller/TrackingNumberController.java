package uk.diasna.tng.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.diasna.tng.dto.TrackingNumberRequest;
import uk.diasna.tng.dto.TrackingNumberResponse;
import uk.diasna.tng.service.TrackingNumberService;
import uk.diasna.tng.validator.TrackingNumberRequestValidator;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class TrackingNumberController {
    
    private static final Logger logger = LoggerFactory.getLogger(TrackingNumberController.class);
    
    private final TrackingNumberService trackingNumberService;
    private final TrackingNumberRequestValidator validator;
    
    public TrackingNumberController(TrackingNumberService trackingNumberService,
                                  TrackingNumberRequestValidator validator) {
        this.trackingNumberService = trackingNumberService;
        this.validator = validator;
    }
    @GetMapping("/next-tracking-number")
    public ResponseEntity<TrackingNumberResponse> generateTrackingNumber(
            @RequestParam("origin_country_id") String originCountryId,
            @RequestParam("destination_country_id") String destinationCountryId,
            @RequestParam("weight") BigDecimal weight,
            @RequestParam("customer_id") UUID customerId,
            @RequestParam("customer_name") String customerName,
            @RequestParam("customer_slug") String customerSlug) {
        
        logger.info("Received tracking number generation request for customer: {} from {} to {}", 
                   customerId, originCountryId, destinationCountryId);
        
        TrackingNumberRequest request = new TrackingNumberRequest(
            originCountryId.toUpperCase(),
            destinationCountryId.toUpperCase(),
            weight,
            customerId,
            customerName,
            customerSlug.toLowerCase()
        );
        
        validator.validate(request);
        
        TrackingNumberResponse response = trackingNumberService.generateTrackingNumber(request);
        
        logger.info("Successfully generated tracking number: {} for customer: {}", 
                   response.trackingNumber(), customerId);
        
        return ResponseEntity.ok(response);
    }
}
