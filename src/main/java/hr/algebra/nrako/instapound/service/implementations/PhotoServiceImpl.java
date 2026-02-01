package hr.algebra.nrako.instapound.service.implementations;

import hr.algebra.nrako.instapound.model.dto.PhotoResponse;
import hr.algebra.nrako.instapound.model.dto.PhotoSearchRequest;
import hr.algebra.nrako.instapound.model.entity.Hashtag;
import hr.algebra.nrako.instapound.model.entity.Photo;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.repository.HashtagRepository;
import hr.algebra.nrako.instapound.repository.PhotoRepository;
import hr.algebra.nrako.instapound.repository.UserRepository;
import hr.algebra.nrako.instapound.service.interfaces.PhotoService;
import hr.algebra.nrako.instapound.specification.PhotoSpecification;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PhotoServiceImpl implements PhotoService {
    private PhotoRepository photoRepository;
    private UserRepository userRepository;
    private HashtagRepository hashtagRepository;

    @Override
    public List<PhotoResponse> getAll() {
        return photoRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PhotoResponse> getById(Long id) {
        Optional<Photo> photo = photoRepository.findById(id);
        return photo.map(this::toDto);
    }

    @Override
    public PhotoResponse save(PhotoResponse photoResponse) throws ParseException {
        Photo photo = photoRepository.save(toEntity(photoResponse));
        return toDto(photo);
    }

    @Override
    public Optional<PhotoResponse> update(PhotoResponse photoResponse) throws ParseException {
        Optional<Photo> existingPhotoOpt = photoRepository.findById(photoResponse.getId());
        if (existingPhotoOpt.isEmpty()) {
            return Optional.empty();
        }
        Photo photo = existingPhotoOpt.get();

        Set<Hashtag> hashtags = new HashSet<>();
        if (photoResponse.getHashtags() != null) {
            for (String tag : photoResponse.getHashtags()) {
                Hashtag hashtag = hashtagRepository.findByTag(tag);
                if (hashtag == null) {
                    hashtag = Hashtag.builder()
                            .withTag(tag)
                            .build();
                    hashtag = hashtagRepository.save(hashtag);
                }
                hashtag.incrementUsage();
                hashtags.add(hashtag);
            }
        }

        photo.setDescription(photoResponse.getDescription());
        photo.setHashtags(hashtags);
        photo.setEditedAt(LocalDateTime.now());

        Photo updatedPhoto = photoRepository.save(photo);
        return Optional.of(toDto(updatedPhoto));
    }

    @Override
    public void deleteById(Long id) {
        if (photoRepository.existsById(id)) {
            photoRepository.deleteById(id);
        }
    }

    @Override
    public List<PhotoResponse> filterByUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return List.of();
        }
        // Use a reasonable page size limit to avoid memory issues
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        return photoRepository.findByUserOrderByUploadedAtDesc(user, pageable)
                .getContent()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PhotoResponse> filterByParams(PhotoSearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(
                searchRequest.getPage() != null ? searchRequest.getPage() : 0,
                searchRequest.getPageSize() != null ? searchRequest.getPageSize() : 10,
                Sort.by(Sort.Direction.DESC, "uploadedAt")
        );
        return photoRepository.findAll(PhotoSpecification.fromSearchRequest(searchRequest), pageable)
                .getContent()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private PhotoResponse toDto(Photo photo) {
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

    private Photo toEntity(PhotoResponse response) throws ParseException {
        Set<Hashtag> hashtags = response.getHashtags() == null ? new HashSet<>()
                : response.getHashtags().stream()
                .map(tag -> Hashtag.builder().withTag(tag).build())
                .collect(Collectors.toSet());
        return Photo.builder()
                .id(response.getId())
                .originalFileName(response.getOriginalFileName())
                .description(response.getDescription())
                .hashtags(hashtags)
                .thumbnailUrl(response.getThumbnailUrl())
                .processedUrl(response.getProcessedUrl())
                .user(userRepository.findById(response.getAuthorId()).get())
                .fileSizeBytes(response.getFileSizeBytes())
                .width(response.getWidth())
                .height(response.getHeight())
                .uploadedAt(response.getUploadedAt())
                .editedAt(response.getEditedAt())
                .downloadCount(response.getDownloadCount())
                .viewCount(response.getViewCount())
                .build();
    }
}
