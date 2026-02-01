package hr.algebra.nrako.instapound.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

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
    private Integer pageSize = 9;
}
