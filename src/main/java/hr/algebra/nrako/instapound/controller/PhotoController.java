package hr.algebra.nrako.instapound.controller;

import hr.algebra.nrako.instapound.enums.ActionType;
import hr.algebra.nrako.instapound.enums.ImageFormat;
import hr.algebra.nrako.instapound.enums.StorageType;
import hr.algebra.nrako.instapound.enums.UserRole;
import hr.algebra.nrako.instapound.model.dto.*;
import hr.algebra.nrako.instapound.model.entity.Hashtag;
import hr.algebra.nrako.instapound.model.entity.Photo;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.entity.UserPackage;
import hr.algebra.nrako.instapound.model.valueobject.ImageProcessingOptions;
import hr.algebra.nrako.instapound.repository.HashtagRepository;
import hr.algebra.nrako.instapound.repository.PhotoRepository;
import hr.algebra.nrako.instapound.repository.UserRepository;
import hr.algebra.nrako.instapound.service.imageprocessing.ImageProcessorService;
import hr.algebra.nrako.instapound.service.interfaces.ActionLogService;
import hr.algebra.nrako.instapound.service.interfaces.UserPackageService;
import hr.algebra.nrako.instapound.service.storage.LocalStorageStrategy;
import hr.algebra.nrako.instapound.service.storage.StorageService;
import hr.algebra.nrako.instapound.specification.PhotoSpecification;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Photo Controller - MVC Pattern (Controller)
 * Handles all photo-related operations
 */
@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
@Slf4j
public class PhotoController {

    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final HashtagRepository hashtagRepository;
    private final StorageService storageService;
    private final LocalStorageStrategy localStorageStrategy;
    private final ImageProcessorService imageProcessorService;
    private final ActionLogService actionLogService;
    private final UserPackageService userPackageService;

    /**
     * Upload a photo - requires REGISTERED or ADMIN role
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('REGISTERED', 'ADMIN')")
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "hashtags", required = false) Set<String> hashtags,
            @RequestParam(value = "storageType", defaultValue = "LOCAL") StorageType storageType,
            @RequestParam(value = "format", required = false) ImageFormat imageFormat,
            @RequestParam(value = "width", required = false) Integer targetWidth,
            @RequestParam(value = "height", required = false) Integer targetHeight,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }

            // Check package limits
            UserPackage userPackage = userPackageService.getPackageByType(user.getPackageType());
            if (userPackage != null && !user.canUpload(file.getSize(), userPackage)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Package limit exceeded. Please upgrade your package or wait until tomorrow.");
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : ".jpg";
            String storedFilename = UUID.randomUUID().toString() + extension;

            // Process image if options provided
            byte[] imageData = file.getBytes();
            if (imageFormat != null || targetWidth != null || targetHeight != null) {
                ImageProcessingOptions options = ImageProcessingOptions.builder()
                        .imageFormat(imageFormat)
                        .width(targetWidth)
                        .height(targetHeight)
                        .build();
                imageData = imageProcessorService.processImage(new ByteArrayInputStream(file.getBytes()), options);
                
                // Update extension based on format
                if (imageFormat != null) {
                    extension = "." + imageFormat.name().toLowerCase();
                    storedFilename = storedFilename.replace(storedFilename.substring(storedFilename.lastIndexOf(".")), extension);
                }
            }

            // Store the file
            String contentType = imageFormat != null ? "image/" + imageFormat.name().toLowerCase() : file.getContentType();
            storageService.store(imageData, storedFilename, contentType, storageType);

            // Create thumbnail
            String thumbnailFilename = "thumb_" + storedFilename;
            byte[] thumbnailData = imageProcessorService.createThumbnail(new ByteArrayInputStream(imageData), 200, 200);
            storageService.store(thumbnailData, thumbnailFilename, "image/jpeg", storageType);

            // Get image dimensions
            int[] dimensions = imageProcessorService.getImageDimensions(imageData);

            // Create hashtags
            Set<Hashtag> photoHashtags = new HashSet<>();
            if (hashtags != null) {
                for (String tag : hashtags) {
                    String normalizedTag = tag.toLowerCase().trim();
                    Hashtag hashtag = hashtagRepository.findByTag(normalizedTag);
                    if (hashtag == null) {
                        hashtag = Hashtag.builder().withTag(normalizedTag).build();
                        hashtagRepository.save(hashtag);
                    }
                    hashtag.incrementUsage();
                    photoHashtags.add(hashtag);
                }
            }

            // Save photo entity
            Photo photo = Photo.builder()
                    .user(user)
                    .originalFileName(originalFilename)
                    .storedFileName(storedFilename)
                    .description(description)
                    .hashtags(photoHashtags)
                    .storageType(storageType)
                    .storageUrl(storageService.getUrl(storedFilename, storageType))
                    .thumbnailUrl(storageService.getUrl(thumbnailFilename, storageType))
                    .fileSizeBytes((long) imageData.length)
                    .width(dimensions[0])
                    .height(dimensions[1])
                    .mimeType(contentType)
                    .uploadedAt(LocalDateTime.now())
                    .downloadCount(0L)
                    .viewCount(0L)
                    .build();

            photoRepository.save(photo);

            // Update user's package usage
            user.recordUpload(imageData.length);
            userRepository.save(user);

            // Log the action
            actionLogService.logAction(user, ActionType.PHOTO_UPLOAD, 
                    "Uploaded photo: " + originalFilename, getClientIp(request), photo.getId());

            log.info("Photo uploaded successfully: {} by {}", storedFilename, user.getUsername());
            return ResponseEntity.ok(mapToResponse(photo));

        } catch (IOException e) {
            log.error("Error uploading photo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading photo: " + e.getMessage());
        }
    }

    /**
     * Browse photos - thumbnails of 10 last uploaded photos
     */
    @GetMapping("/browse")
    public ResponseEntity<Page<PhotoResponse>> browsePhotos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        Page<Photo> photos = photoRepository.findAllByOrderByUploadedAtDesc(pageable);
        
        actionLogService.logAnonymousAction(ActionType.PHOTO_VIEW, 
                "Browsed photos page " + page, getClientIp(request));
        
        return ResponseEntity.ok(photos.map(this::mapToResponse));
    }

    /**
     * Search photos with filters
     */
    @PostMapping("/search")
    public ResponseEntity<Page<PhotoResponse>> searchPhotos(
            @RequestBody PhotoSearchRequest searchRequest,
            HttpServletRequest request) {
        
        Pageable pageable = PageRequest.of(
                searchRequest.getPage() != null ? searchRequest.getPage() : 0,
                searchRequest.getPageSize() != null ? searchRequest.getPageSize() : 10,
                Sort.by(Sort.Direction.DESC, "uploadedAt")
        );
        
        Page<Photo> photos = photoRepository.findAll(
                PhotoSpecification.fromSearchRequest(searchRequest), pageable);
        
        actionLogService.logAnonymousAction(ActionType.USER_SEARCH, 
                "Searched photos with filters", getClientIp(request));
        
        return ResponseEntity.ok(photos.map(this::mapToResponse));
    }

    /**
     * Get a single photo by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PhotoResponse> getPhoto(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        Optional<Photo> photoOpt = photoRepository.findById(id);
        if (photoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Photo photo = photoOpt.get();
        photo.incrementViewCount();
        photoRepository.save(photo);
        
        actionLogService.logAnonymousAction(ActionType.PHOTO_VIEW, 
                "Viewed photo: " + id, getClientIp(request));
        
        return ResponseEntity.ok(mapToResponse(photo));
    }

    /**
     * Edit photo description and hashtags - requires owner or ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('REGISTERED', 'ADMIN')")
    public ResponseEntity<?> editPhoto(
            @PathVariable Long id,
            @RequestBody PhotoEditRequest editRequest,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        Optional<Photo> photoOpt = photoRepository.findById(id);
        if (photoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Photo photo = photoOpt.get();

        // Check if user owns the photo or is admin
        if (!photo.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only edit your own photos");
        }

        // Update description
        if (editRequest.getDescription() != null) {
            photo.setDescription(editRequest.getDescription());
        }

        // Update hashtags
        if (editRequest.getHashtags() != null) {
            Set<Hashtag> newHashtags = new HashSet<>();
            for (String tag : editRequest.getHashtags()) {
                String normalizedTag = tag.toLowerCase().trim();
                Hashtag hashtag = hashtagRepository.findByTag(normalizedTag);
                if (hashtag == null) {
                    hashtag = Hashtag.builder().withTag(normalizedTag).build();
                    hashtagRepository.save(hashtag);
                }
                hashtag.incrementUsage();
                newHashtags.add(hashtag);
            }
            photo.setHashtags(newHashtags);
        }

        photo.setEditedAt(LocalDateTime.now());
        photoRepository.save(photo);

        actionLogService.logAction(user, ActionType.PHOTO_EDIT, 
                "Edited photo: " + id, getClientIp(request), id);

        return ResponseEntity.ok(mapToResponse(photo));
    }

    /**
     * Delete a photo - requires owner or ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('REGISTERED', 'ADMIN')")
    public ResponseEntity<?> deletePhoto(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        Optional<Photo> photoOpt = photoRepository.findById(id);
        if (photoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Photo photo = photoOpt.get();

        // Check if user owns the photo or is admin
        if (!photo.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only delete your own photos");
        }

        try {
            // Delete from storage
            storageService.delete(photo.getStoredFileName(), photo.getStorageType());
            
            // Delete thumbnail
            String thumbnailFilename = "thumb_" + photo.getStoredFileName();
            try {
                storageService.delete(thumbnailFilename, photo.getStorageType());
            } catch (Exception e) {
                log.warn("Could not delete thumbnail: {}", thumbnailFilename);
            }

            // Delete from database
            photoRepository.delete(photo);

            actionLogService.logAction(user, ActionType.PHOTO_DELETE, 
                    "Deleted photo: " + id, getClientIp(request), id);

            return ResponseEntity.ok().body("Photo deleted successfully");

        } catch (IOException e) {
            log.error("Error deleting photo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting photo: " + e.getMessage());
        }
    }

    /**
     * Download a photo - can choose original or with filters
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadPhoto(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean original,
            @RequestParam(required = false) ImageFormat format,
            @RequestParam(required = false) Integer width,
            @RequestParam(required = false) Integer height,
            @RequestParam(required = false) Set<String> filters,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        
        Optional<Photo> photoOpt = photoRepository.findById(id);
        if (photoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Photo photo = photoOpt.get();

        try {
            InputStream inputStream = storageService.retrieve(photo.getStoredFileName(), photo.getStorageType());
            byte[] imageData;

            if (!original && (format != null || width != null || height != null || 
                    (filters != null && !filters.isEmpty()))) {
                // Process image with filters
                Set<hr.algebra.nrako.instapound.enums.ImageFilter> imageFilters = filters != null 
                        ? filters.stream()
                            .map(f -> hr.algebra.nrako.instapound.enums.ImageFilter.valueOf(f.toUpperCase()))
                            .collect(Collectors.toSet())
                        : new HashSet<>();

                ImageProcessingOptions options = ImageProcessingOptions.builder()
                        .imageFormat(format)
                        .width(width)
                        .height(height)
                        .filters(imageFilters)
                        .build();

                imageData = imageProcessorService.processImage(inputStream, options);
            } else {
                imageData = inputStream.readAllBytes();
            }

            photo.incrementDownloadCount();
            photoRepository.save(photo);

            // Log action
            User user = userDetails != null ? userRepository.findByUsername(userDetails.getUsername()) : null;
            if (user != null) {
                actionLogService.logAction(user, ActionType.PHOTO_DOWNLOAD, 
                        "Downloaded photo: " + id + (original ? " (original)" : " (processed)"), 
                        getClientIp(request), id);
            } else {
                actionLogService.logAnonymousAction(ActionType.PHOTO_DOWNLOAD, 
                        "Downloaded photo: " + id, getClientIp(request));
            }

            String contentType = format != null ? "image/" + format.name().toLowerCase() : photo.getMimeType();
            String filename = format != null 
                    ? photo.getOriginalFileName().replace(
                        photo.getOriginalFileName().substring(photo.getOriginalFileName().lastIndexOf(".")),
                        "." + format.name().toLowerCase())
                    : photo.getOriginalFileName();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(new InputStreamResource(new ByteArrayInputStream(imageData)));

        } catch (IOException e) {
            log.error("Error downloading photo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Serve file directly (for thumbnails and images)
     */
    @GetMapping("/file/{filename}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            InputStream inputStream = localStorageStrategy.retrieve(filename);
            String contentType = filename.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new InputStreamResource(inputStream));
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get photos by user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PhotoResponse>> getPhotosByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        Page<Photo> photos = photoRepository.findByUserOrderByUploadedAtDesc(userOpt.get(), pageable);
        
        return ResponseEntity.ok(photos.map(this::mapToResponse));
    }

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

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
