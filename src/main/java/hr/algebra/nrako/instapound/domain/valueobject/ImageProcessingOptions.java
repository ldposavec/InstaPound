package hr.algebra.nrako.instapound.domain.valueobject;

import hr.algebra.nrako.instapound.domain.enums.ImageFilter;
import hr.algebra.nrako.instapound.domain.enums.ImageFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Value Object Pattern (Design Pattern #1)
 * Encapsulates image processing configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageProcessingOptions {
    private ImageFormat format;
    private Integer width;
    private Integer height;
    private Set<ImageFilter> filters = new HashSet<>();
    private Integer quality;  // 1-100 for compression quality
}
