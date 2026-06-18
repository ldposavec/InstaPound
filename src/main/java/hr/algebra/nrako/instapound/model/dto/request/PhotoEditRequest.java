package hr.algebra.nrako.instapound.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoEditRequest {
    private String description;
    private Set<String> hashtags;
}
