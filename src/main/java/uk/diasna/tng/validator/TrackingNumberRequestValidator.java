package uk.diasna.tng.validator;

import org.springframework.stereotype.Component;
import uk.diasna.tng.dto.TrackingNumberRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class TrackingNumberRequestValidator {
    
    private static final String ISO_COUNTRY_CODE_PATTERN = "^[A-Z]{2}$";
    private static final String KEBAB_CASE_PATTERN = "^[a-z0-9-]+$";
    private static final BigDecimal MIN_WEIGHT = new BigDecimal("0.001");
    private static final BigDecimal MAX_WEIGHT = new BigDecimal("999999.999");
    
    /**
     * Validates a tracking number request and throws IllegalArgumentException with detailed message
     */
    public void validate(TrackingNumberRequest request) {
        List<String> errors = collectValidationErrors(request);
        
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }
    }
    
    /**
     * Validates and returns a list of all validation errors (useful for comprehensive error reporting)
     */
    public List<String> collectValidationErrors(TrackingNumberRequest request) {
        List<String> errors = new ArrayList<>();
        
        validateOriginCountryId(request.originCountryId(), errors);
        validateDestinationCountryId(request.destinationCountryId(), errors);
        validateWeight(request.weight(), errors);
        validateCustomerId(request.customerId(), errors);
        validateCustomerName(request.customerName(), errors);
        validateCustomerSlug(request.customerSlug(), errors);
        
        return errors;
    }
    
    /**
     * Checks if the request is valid without throwing exceptions
     */
    public boolean isValid(TrackingNumberRequest request) {
        return collectValidationErrors(request).isEmpty();
    }
    
    private void validateOriginCountryId(String originCountryId, List<String> errors) {
        if (isNullOrEmpty(originCountryId)) {
            errors.add("Origin country ID is required");
        } else if (!originCountryId.matches(ISO_COUNTRY_CODE_PATTERN)) {
            errors.add("Origin country ID must be ISO 3166-1 alpha-2 format (e.g., 'US', 'GB')");
        }
    }
    
    private void validateDestinationCountryId(String destinationCountryId, List<String> errors) {
        if (isNullOrEmpty(destinationCountryId)) {
            errors.add("Destination country ID is required");
        } else if (!destinationCountryId.matches(ISO_COUNTRY_CODE_PATTERN)) {
            errors.add("Destination country ID must be ISO 3166-1 alpha-2 format (e.g., 'US', 'GB')");
        }
    }
    
    private void validateWeight(BigDecimal weight, List<String> errors) {
        if (weight == null) {
            errors.add("Weight is required");
        } else {
            if (weight.compareTo(MIN_WEIGHT) < 0) {
                errors.add("Weight must be at least " + MIN_WEIGHT + " kg");
            }
            if (weight.compareTo(MAX_WEIGHT) > 0) {
                errors.add("Weight must not exceed " + MAX_WEIGHT + " kg (1 million kg limit)");
            }
        }
    }
    
    private void validateCustomerId(Object customerId, List<String> errors) {
        if (customerId == null) {
            errors.add("Customer ID is required");
        }
    }
    
    private void validateCustomerName(String customerName, List<String> errors) {
        if (isNullOrEmpty(customerName)) {
            errors.add("Customer name is required");
        } else if (customerName.trim().isEmpty()) {
            errors.add("Customer name cannot be blank");
        }
    }
    
    private void validateCustomerSlug(String customerSlug, List<String> errors) {
        if (isNullOrEmpty(customerSlug)) {
            errors.add("Customer slug is required");
        } else if (!customerSlug.matches(KEBAB_CASE_PATTERN)) {
            errors.add("Customer slug must be in kebab-case format (lowercase letters, numbers, and hyphens only)");
        }
    }
    
    private boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }
}
