package hr.algebra.nrako.instapound.dto;

import hr.algebra.nrako.instapound.domain.enums.ImageFilter;
import hr.algebra.nrako.instapound.domain.enums.ImageFormat;
import hr.algebra.nrako.instapound.domain.enums.StorageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * DTO for photo upload request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoUploadRequest {
    
    private String description;
    
    private Set<String> hashtags = new HashSet<>();
    
    private StorageType storageType = StorageType.LOCAL;
    
    private ImageFormat targetFormat;
    
    private Integer targetWidth;
    
    private Integer targetHeight;
    
    private Set<ImageFilter> filters = new HashSet<>();
    
    private Integer quality = 85;
}
