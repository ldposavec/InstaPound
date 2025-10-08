package hr.algebra.nrako.instapound.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for photo response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoResponse {
    
    private Long id;
    
    private String originalFileName;
    
    private String description;
    
    private Set<String> hashtags;
    
    private String thumbnailUrl;
    
    private String imageUrl;
    
    private String processedUrl;
    
    private String author;
    
    private Long authorId;
    
    private Long fileSizeBytes;
    
    private Integer width;
    
    private Integer height;
    
    private LocalDateTime uploadedAt;
    
    private LocalDateTime editedAt;
    
    private Long downloadCount;
    
    private Long viewCount;
}
