package hr.algebra.nrako.instapound.model.valueobject;

import hr.algebra.nrako.instapound.enums.ImageFilter;
import hr.algebra.nrako.instapound.enums.ImageFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageProcessingOptions {
    private ImageFormat imageFormat;
    private Integer width;
    private Integer height;
    private Integer quality;
    @Builder.Default
    private Set<ImageFilter> filters = new HashSet<>();
}
