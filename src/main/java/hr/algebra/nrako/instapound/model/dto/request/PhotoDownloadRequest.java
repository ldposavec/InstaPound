package hr.algebra.nrako.instapound.model.dto.request;

import hr.algebra.nrako.instapound.enums.ImageFilter;
import hr.algebra.nrako.instapound.enums.ImageFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDownloadRequest {
    @Builder.Default
    private boolean original = true;
    private ImageFormat format;
    private Integer width;
    private Integer height;
    private Set<ImageFilter> filters;
}
