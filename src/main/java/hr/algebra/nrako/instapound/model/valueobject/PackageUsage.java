package hr.algebra.nrako.instapound.model.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageUsage {
    private Integer currentPhotoCount = 0;
    private Integer todayUploadCount = 0;
    private LocalDate lastUploadDate;
    private Long totalStorageUsed = 0L;
}
