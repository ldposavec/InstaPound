package hr.algebra.nrako.instapound.service;

import hr.algebra.nrako.instapound.domain.entity.Hashtag;
import hr.algebra.nrako.instapound.domain.entity.Photo;
import hr.algebra.nrako.instapound.domain.entity.User;
import hr.algebra.nrako.instapound.domain.entity.UserPackage;
import hr.algebra.nrako.instapound.domain.enums.ActionType;
import hr.algebra.nrako.instapound.domain.valueobject.ImageProcessingOptions;
import hr.algebra.nrako.instapound.dto.PhotoResponse;
import hr.algebra.nrako.instapound.dto.PhotoSearchRequest;
import hr.algebra.nrako.instapound.dto.PhotoUploadRequest;
import hr.algebra.nrako.instapound.exception.PackageLimitExceededException;
import hr.algebra.nrako.instapound.exception.ResourceNotFoundException;
import hr.algebra.nrako.instapound.exception.UnauthorizedException;
import hr.algebra.nrako.instapound.repository.HashtagRepository;
import hr.algebra.nrako.instapound.repository.PhotoRepository;
import hr.algebra.nrako.instapound.util.ImageProcessor;
import hr.algebra.nrako.instapound.util.StorageService;
import hr.algebra.nrako.instapound.util.StorageServiceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for photo management
 * Adheres to Single Responsibility Principle (SOLID)
 * Implements business logic for photo operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoService {
    
    private final PhotoRepository photoRepository;
    private final HashtagRepository hashtagRepository;
    private final UserService userService;
    private final ImageProcessor imageProcessor;
    private final StorageServiceFactory storageServiceFactory;
    private final ActionLogger actionLogger;
    
    /**
     * Upload a new photo
     * 
     * @param file Image file
     * @param request Upload request with metadata and processing options
     * @param userId User ID
     * @return Created photo response
     */
    @Transactional
    public PhotoResponse uploadPhoto(MultipartFile file, PhotoUploadRequest request, Long userId) 
            throws IOException {
        User user = userService.findById(userId);
        UserPackage userPackage = userService.getUserPackage(user);
        
        // Check package limits
        if (!user.canUpload(file.getSize(), userPackage)) {
            throw new PackageLimitExceededException("Package limits exceeded");
        }
        
        // Process image
        ImageProcessingOptions options = ImageProcessingOptions.builder()
            .format(request.getTargetFormat())
            .width(request.getTargetWidth())
            .height(request.getTargetHeight())
            .filters(request.getFilters())
            .quality(request.getQuality())
            .build();
        
        byte[] processedImage = (options.getFormat() != null || 
                                 options.getWidth() != null || 
                                 !options.getFilters().isEmpty()) 
            ? imageProcessor.processImage(file, options) 
            : file.getBytes();
        
        // Generate thumbnail
        byte[] thumbnail = imageProcessor.generateThumbnail(file, 200, 200);
        
        // Store files
        String uniqueId = UUID.randomUUID().toString();
        StorageService storageService = storageServiceFactory.getStorageService(request.getStorageType());
        
        String originalUrl = storageService.store(uniqueId + "_original", file.getBytes());
        String processedUrl = storageService.store(uniqueId + "_processed", processedImage);
        String thumbnailUrl = storageService.store(uniqueId + "_thumb", thumbnail);
        
        // Create photo entity
        Photo photo = Photo.builder()
            .user(user)
            .originalFileName(file.getOriginalFilename())
            .storedFileName(uniqueId)
            .description(request.getDescription())
            .storageType(request.getStorageType())
            .storageUrl(originalUrl)
            .processedUrl(processedUrl)
            .thumbnailUrl(thumbnailUrl)
            .fileSizeBytes(file.getSize())
            .mimeType(file.getContentType())
            .build();
        
        // Process hashtags
        if (request.getHashtags() != null) {
            for (String tag : request.getHashtags()) {
                Hashtag hashtag = hashtagRepository.findByTag(tag)
                    .orElseGet(() -> {
                        Hashtag newTag = Hashtag.builder()
                            .tag(tag)
                            .build();
                        return hashtagRepository.save(newTag);
                    });
                hashtag.incrementUsageCount();
                photo.addHashtag(hashtag);
            }
        }
        
        photo = photoRepository.save(photo);
        
        // Update user's package usage
        user.recordUpload(file.getSize());
        
        actionLogger.logAction(user, ActionType.PHOTO_UPLOAD, 
            "Photo uploaded: " + file.getOriginalFilename(), null, photo.getId(), null);
        
        log.info("Photo uploaded by user {}: {}", user.getUsername(), photo.getOriginalFileName());
        
        return mapToResponse(photo);
    }
    
    /**
     * Get photo by ID
     * 
     * @param photoId Photo ID
     * @return Photo entity
     */
    public Photo findById(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
            .orElseThrow(() -> new ResourceNotFoundException("Photo", "id", photoId));
        
        // Increment view count
        photo.incrementViewCount();
        photoRepository.save(photo);
        
        return photo;
    }
    
    /**
     * Get photo by ID as response DTO
     * 
     * @param photoId Photo ID
     * @return Photo response
     */
    public PhotoResponse getPhoto(Long photoId) {
        Photo photo = findById(photoId);
        return mapToResponse(photo);
    }
    
    /**
     * Get latest photos (default view)
     * 
     * @param page Page number
     * @param size Page size
     * @return Page of photo responses
     */
    public Page<PhotoResponse> getLatestPhotos(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        return photoRepository.findAll(pageable).map(this::mapToResponse);
    }
    
    /**
     * Search photos based on criteria
     * 
     * @param searchRequest Search criteria
     * @return Page of photo responses
     */
    public Page<PhotoResponse> searchPhotos(PhotoSearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(
            searchRequest.getPage(), 
            searchRequest.getSize(), 
            Sort.by(Sort.Direction.DESC, "uploadedAt")
        );
        
        return photoRepository.findAll(
            PhotoSpecifications.fromSearchRequest(searchRequest), 
            pageable
        ).map(this::mapToResponse);
    }
    
    /**
     * Update photo metadata (description and hashtags)
     * 
     * @param photoId Photo ID
     * @param description New description
     * @param hashtags New hashtags
     * @param userId User ID requesting update
     * @return Updated photo response
     */
    @Transactional
    public PhotoResponse updatePhoto(Long photoId, String description, Set<String> hashtags, Long userId) {
        Photo photo = findById(photoId);
        User user = userService.findById(userId);
        
        // Check authorization
        if (!photo.getUser().getId().equals(userId) && user.getRole() != hr.algebra.nrako.instapound.domain.enums.UserRole.ADMINISTRATOR) {
            throw new UnauthorizedException("You don't have permission to edit this photo");
        }
        
        // Update description
        if (description != null) {
            photo.setDescription(description);
        }
        
        // Update hashtags
        if (hashtags != null) {
            // Remove old hashtags
            photo.getHashtags().clear();
            
            // Add new hashtags
            for (String tag : hashtags) {
                Hashtag hashtag = hashtagRepository.findByTag(tag)
                    .orElseGet(() -> {
                        Hashtag newTag = Hashtag.builder()
                            .tag(tag)
                            .build();
                        return hashtagRepository.save(newTag);
                    });
                hashtag.incrementUsageCount();
                photo.addHashtag(hashtag);
            }
        }
        
        photo.setEditedAt(LocalDateTime.now());
        photo = photoRepository.save(photo);
        
        actionLogger.logAction(user, ActionType.PHOTO_EDIT, 
            "Photo edited: " + photo.getOriginalFileName(), null, photo.getId(), null);
        
        log.info("Photo {} updated by user {}", photo.getId(), user.getUsername());
        
        return mapToResponse(photo);
    }
    
    /**
     * Delete a photo
     * 
     * @param photoId Photo ID
     * @param userId User ID requesting deletion
     */
    @Transactional
    public void deletePhoto(Long photoId, Long userId) throws IOException {
        Photo photo = findById(photoId);
        User user = userService.findById(userId);
        
        // Check authorization
        if (!photo.getUser().getId().equals(userId) && user.getRole() != hr.algebra.nrako.instapound.domain.enums.UserRole.ADMINISTRATOR) {
            throw new UnauthorizedException("You don't have permission to delete this photo");
        }
        
        // Delete files from storage
        StorageService storageService = storageServiceFactory.getStorageService(photo.getStorageType());
        try {
            storageService.delete(photo.getStorageUrl());
            if (photo.getProcessedUrl() != null) {
                storageService.delete(photo.getProcessedUrl());
            }
            if (photo.getThumbnailUrl() != null) {
                storageService.delete(photo.getThumbnailUrl());
            }
        } catch (Exception e) {
            log.warn("Failed to delete photo files from storage: {}", e.getMessage());
        }
        
        // Update user's package usage
        User photoOwner = photo.getUser();
        photoOwner.getPackageUsage().setCurrentPhotoCount(
            photoOwner.getPackageUsage().getCurrentPhotoCount() - 1);
        photoOwner.getPackageUsage().setTotalStorageUsed(
            photoOwner.getPackageUsage().getTotalStorageUsed() - photo.getFileSizeBytes());
        
        photoRepository.delete(photo);
        
        actionLogger.logAction(user, ActionType.PHOTO_DELETE, 
            "Photo deleted: " + photo.getOriginalFileName(), null, photoId, null);
        
        log.info("Photo {} deleted by user {}", photoId, user.getUsername());
    }
    
    /**
     * Download a photo
     * 
     * @param photoId Photo ID
     * @param processed Whether to download processed version
     * @param userId User ID (can be null for anonymous)
     * @return Photo data
     */
    @Transactional
    public byte[] downloadPhoto(Long photoId, boolean processed, Long userId) throws IOException {
        Photo photo = findById(photoId);
        StorageService storageService = storageServiceFactory.getStorageService(photo.getStorageType());
        
        String fileUrl = processed && photo.getProcessedUrl() != null 
            ? photo.getProcessedUrl() 
            : photo.getStorageUrl();
        
        photo.incrementDownloadCount();
        photoRepository.save(photo);
        
        if (userId != null) {
            User user = userService.findById(userId);
            actionLogger.logAction(user, ActionType.PHOTO_DOWNLOAD, 
                "Photo downloaded: " + photo.getOriginalFileName(), null, photo.getId(), null);
        } else {
            actionLogger.logAnonymousAction(ActionType.PHOTO_DOWNLOAD, 
                "Photo downloaded: " + photo.getOriginalFileName(), null);
        }
        
        return storageService.retrieve(fileUrl);
    }
    
    /**
     * Map Photo entity to PhotoResponse DTO
     */
    private PhotoResponse mapToResponse(Photo photo) {
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
