package hr.algebra.nrako.instapound.model.mappers;

import hr.algebra.nrako.instapound.model.dto.response.PhotoResponse;
import hr.algebra.nrako.instapound.model.entity.Hashtag;
import hr.algebra.nrako.instapound.model.entity.Photo;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PhotoMapper {
    public PhotoResponse toDto(Photo photo) {
        return PhotoResponse.builder()
                .id(photo.getId())
                .originalFileName(photo.getOriginalFileName())
                .description(photo.getDescription())
                .hashtags(photo.getHashtags().stream()
                        .map(Hashtag::getTag)
                        .collect(Collectors.toSet()))
                .thumbnailUrl(photo.getThumbnailUrl())
                .imageUrl(photo.getStorageUrl())
                .processedUrl(photo.getProcessedUrl())
                .author(photo.getUser().getUsername())
                .authorId(photo.getUser().getId())
                .fileSizeBytes(photo.getFileSizeBytes())
                .width(photo.getWidth())
                .height(photo.getHeight())
                .uploadedAt(photo.getUploadedAt())
                .editedAt(photo.getEditedAt())
                .downloadCount(photo.getDownloadCount())
                .viewCount(photo.getViewCount())
                .build();
    }
}
