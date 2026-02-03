package hr.algebra.nrako.instapound.model.dto.request;

import hr.algebra.nrako.instapound.enums.ImageFilter;
import hr.algebra.nrako.instapound.enums.ImageFormat;
import hr.algebra.nrako.instapound.enums.StorageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoUploadRequest {
    private String description;
    private Set<String> hashtags = new HashSet<>();
    private StorageType storageType = StorageType.LOCAL;
    private ImageFormat imageFormat;
    private Integer targetWidth;
    private Integer targetHeight;
    private Set<ImageFilter> filters = new HashSet<>();
    private Integer quality = 85;
}
