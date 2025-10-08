package hr.algebra.nrako.instapound.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for photo search criteria
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoSearchRequest {
    
    private Set<String> hashtags;
    
    private String author;
    
    private Long authorId;
    
    private LocalDateTime uploadedAfter;
    
    private LocalDateTime uploadedBefore;
    
    private Long minSizeBytes;
    
    private Long maxSizeBytes;
    
    private Integer page = 0;
    
    private Integer size = 10;
}
