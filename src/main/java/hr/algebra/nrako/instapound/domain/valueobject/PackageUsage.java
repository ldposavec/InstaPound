package hr.algebra.nrako.instapound.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Value Object Pattern (Design Pattern #1)
 * Tracks current package usage/consumption
 * Adheres to Single Responsibility Principle (SOLID)
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageUsage {
    private Integer currentPhotoCount = 0;    // Current number of photos uploaded
    private Integer todayUploadCount = 0;     // Number of uploads today
    private LocalDate lastUploadDate;         // Last upload date for daily reset
    private Long totalStorageUsed = 0L;       // Total storage used in bytes
}
