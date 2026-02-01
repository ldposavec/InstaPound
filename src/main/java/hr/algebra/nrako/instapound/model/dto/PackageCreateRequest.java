package hr.algebra.nrako.instapound.model.dto;

import hr.algebra.nrako.instapound.enums.PackageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageCreateRequest {
    private PackageType packageType;
    private String name;
    private String description;
    private Long maxUploadSizeInBytes;
    private Integer dailyUploadLimit;
    private Integer maxTotalPhotos;
    private BigDecimal price;
}
