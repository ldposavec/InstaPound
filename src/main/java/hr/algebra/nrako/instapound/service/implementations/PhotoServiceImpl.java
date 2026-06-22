package hr.algebra.nrako.instapound.service.implementations;

import org.springframework.data.domain.*;
import java.util.*;
import hr.algebra.nrako.instapound.model.dto.response.PhotoResponse;
import hr.algebra.nrako.instapound.model.dto.request.PhotoSearchRequest;
import hr.algebra.nrako.instapound.model.entity.Hashtag;
import hr.algebra.nrako.instapound.model.entity.Photo;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.mappers.PhotoMapper;
import hr.algebra.nrako.instapound.repository.HashtagRepository;
import hr.algebra.nrako.instapound.repository.PhotoRepository;
import hr.algebra.nrako.instapound.repository.UserRepository;
import hr.algebra.nrako.instapound.service.interfaces.PhotoService;
import hr.algebra.nrako.instapound.specification.PhotoSpecification;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PhotoServiceImpl implements PhotoService {
    private PhotoRepository photoRepository;
    private UserRepository userRepository;
    private HashtagRepository hashtagRepository;
    private PhotoMapper photoMapper;

    @Override
    public List<PhotoResponse> getAll() {
        return photoRepository.findAll()
                .stream()
                .map(photoMapper::toDto)
                .toList();
    }

    @Override
    public Optional<PhotoResponse> getById(Long id) {
        Optional<Photo> photo = photoRepository.findById(id);
        return photo.map(photoMapper::toDto);
    }

    @Override
    @Transactional
    public PhotoResponse save(PhotoResponse photoResponse) throws ParseException {
        Photo photo = photoRepository.save(toEntity(photoResponse));
        return photoMapper.toDto(photo);
    }

    @Override
    @Transactional
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
        return Optional.of(photoMapper.toDto(updatedPhoto));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Optional<Photo> photo = photoRepository.findById(id);
        photo.ifPresent(value -> photoRepository.delete(value));
    }

    @Override
    public Page<PhotoResponse> filterByUser(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username);
        if (user == null) return Page.empty(pageable);

        return photoRepository.findByUserOrderByUploadedAtDesc(user, pageable).map(photoMapper::toDto);
    }

    @Override
    public Page<PhotoResponse> filterByParams(PhotoSearchRequest searchRequest, Pageable pageable) {
        Specification<Photo> masterSpecification = Specification.unrestricted();

        if (searchRequest != null) {
            masterSpecification = masterSpecification.and(PhotoSpecification.fromSearchRequest(searchRequest));
        }

        return photoRepository.findAll(masterSpecification, pageable).map(photoMapper::toDto);
    }

    private Photo toEntity(PhotoResponse response) throws ParseException {
        Set<Hashtag> hashtags = response.getHashtags() == null ? new HashSet<>()
                : response.getHashtags().stream()
                .map(tag -> Hashtag.builder().withTag(tag).build())
                .collect(Collectors.toSet());
        User user = userRepository.findById(response.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + response.getAuthorId()));
        return Photo.builder()
                .id(response.getId())
                .originalFileName(response.getOriginalFileName())
                .description(response.getDescription())
                .hashtags(hashtags)
                .thumbnailUrl(response.getThumbnailUrl())
                .processedUrl(response.getProcessedUrl())
                .user(user)
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
