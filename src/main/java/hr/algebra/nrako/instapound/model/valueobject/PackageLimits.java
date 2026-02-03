package hr.algebra.nrako.instapound.model.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageLimits {
    private Long maxUploadSizeInBytes;
    private Integer dailyUploadLimit;
    private Integer maxTotalPhotos;
    private BigDecimal price;
}
