package hr.algebra.nrako.instapound.model.serialization;

import hr.algebra.nrako.instapound.enums.StorageType;
import hr.algebra.nrako.instapound.model.entity.Hashtag;
import hr.algebra.nrako.instapound.model.entity.Photo;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.dto.response.PhotoResponse;
import lombok.Getter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class PhotoSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Long photoId;
    private final Long authorId;
    private final String authorUsername;
    private final String originalFileName;
    private final String description;
    private final Set<String> hashtags;
    private final StorageType storageType;
    private final String storageUrl;
    private final String thumbnailUrl;
    private final String processedUrl;
    private final Long fileSizeBytes;

    public PhotoSnapshot(
            Long photoId,
            Long authorId,
            String authorUsername,
            String originalFileName,
            String description,
            Set<String> hashtags,
            StorageType storageType,
            String storageUrl,
            String thumbnailUrl,
            String processedUrl,
            Long fileSizeBytes
    ) {
        this.photoId = photoId;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.originalFileName = originalFileName;
        this.description = description;
        this.hashtags = hashtags == null ? new HashSet<>() : new HashSet<>(hashtags);
        this.storageType = storageType;
        this.storageUrl = storageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.processedUrl = processedUrl;
        this.fileSizeBytes = fileSizeBytes;
    }

    public static PhotoSnapshot fromPhoto(Photo photo) {
        User user = photo.getUser();
        Set<String> tags = photo.getHashtags() == null ? new HashSet<>() : photo.getHashtags().stream()
                .map(Hashtag::getTag)
                .collect(Collectors.toCollection(HashSet::new));

        return new PhotoSnapshot(
                photo.getId(),
                user != null ? user.getId() : null,
                user != null ? user.getUsername() : null,
                photo.getOriginalFileName(),
                photo.getDescription(),
                tags,
                photo.getStorageType(),
                photo.getStorageUrl(),
                photo.getThumbnailUrl(),
                photo.getProcessedUrl(),
                photo.getFileSizeBytes()
        );
    }

    public PhotoResponse toResponse() {
        return PhotoResponse.builder()
                .id(photoId)
                .authorId(authorId)
                .author(authorUsername)
                .originalFileName(originalFileName)
                .description(description)
                .hashtags(new HashSet<>(hashtags))
                .imageUrl(storageUrl)
                .thumbnailUrl(thumbnailUrl)
                .processedUrl(processedUrl)
                .fileSizeBytes(fileSizeBytes)
                .build();
    }

//    public Long getPhotoId() {
//        return photoId;
//    }
//
//    public Long getAuthorId() {
//        return authorId;
//    }
//
//    public String getAuthorUsername() {
//        return authorUsername;
//    }
//
//    public String getOriginalFileName() {
//        return originalFileName;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public Set<String> getHashtags() {
//        return new HashSet<>(hashtags);
//    }
//
//    public StorageType getStorageType() {
//        return storageType;
//    }
//
//    public String getStorageUrl() {
//        return storageUrl;
//    }
//
//    public String getThumbnailUrl() {
//        return thumbnailUrl;
//    }
//
//    public String getProcessedUrl() {
//        return processedUrl;
//    }
//
//    public Long getFileSizeBytes() {
//        return fileSizeBytes;
//    }
}

