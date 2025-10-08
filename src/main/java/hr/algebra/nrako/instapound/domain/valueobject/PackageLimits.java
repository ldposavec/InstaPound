package hr.algebra.nrako.instapound.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Value Object Pattern (Design Pattern #1)
 * Immutable value object representing package limitations
 * Adheres to Single Responsibility Principle (SOLID)
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageLimits {
    private Long maxUploadSizeBytes;    // Maximum size per upload in bytes
    private Integer dailyUploadLimit;   // Maximum uploads per day
    private Integer maxTotalPhotos;     // Maximum total photos allowed
    private BigDecimal price;           // Package price
}
