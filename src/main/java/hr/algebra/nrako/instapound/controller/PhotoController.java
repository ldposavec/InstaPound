package hr.algebra.nrako.instapound.controller;

import hr.algebra.nrako.instapound.enums.*;
import hr.algebra.nrako.instapound.model.dto.request.PhotoEditRequest;
import hr.algebra.nrako.instapound.model.dto.request.PhotoSearchRequest;
import hr.algebra.nrako.instapound.model.dto.response.PhotoResponse;
import hr.algebra.nrako.instapound.model.entity.Hashtag;
import hr.algebra.nrako.instapound.model.entity.Photo;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.entity.UserPackage;
import hr.algebra.nrako.instapound.model.mappers.PhotoMapper;
import hr.algebra.nrako.instapound.model.valueobject.ImageProcessingOptions;
import hr.algebra.nrako.instapound.repository.HashtagRepository;
import hr.algebra.nrako.instapound.repository.PhotoRepository;
import hr.algebra.nrako.instapound.repository.UserRepository;
import hr.algebra.nrako.instapound.service.implementations.ImageProcessorServiceImpl;
import hr.algebra.nrako.instapound.service.interfaces.ActionLogService;
import hr.algebra.nrako.instapound.service.interfaces.UserPackageService;
import hr.algebra.nrako.instapound.service.storage.StorageServiceImpl;
import hr.algebra.nrako.instapound.specification.PhotoSpecification;
import hr.algebra.nrako.instapound.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/photos")
@AllArgsConstructor
@Slf4j
public class PhotoController {
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final HashtagRepository hashtagRepository;
    private final StorageServiceImpl storageService;
    private final ImageProcessorServiceImpl imageProcessorService;
    private final ActionLogService actionLogService;
    private final UserPackageService userPackageService;
    private final PhotoMapper photoMapper;
    private final IpUtils ipUtils;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('REGISTERED', 'ADMIN')")
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "hashtags", required = false) Set<String> hashtags,
            @RequestParam(value = "storageType", defaultValue = "LOCAL") StorageType storageType,
            @RequestParam(value = "format", required = false) ImageFormat imageFormat,
            @RequestParam(value = "filters", required = false) Set<String> filters,
            @RequestParam(value = "width", required = false) Integer targetWidth,
            @RequestParam(value = "height", required = false) Integer targetHeight,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        try {
            User user = userRepository.findByUsername(userDetails.getUsername());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }

            UserPackage userPackage = userPackageService.getPackageByType(user.getPackageType());
            if (userPackage != null && !user.canUpload(file.getSize(), userPackage)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Package limit exceeded. Please upgrade your package or wait until tomorrow");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf('.')) : ".jpeg";
            String storedFilename = UUID.randomUUID().toString() + extension;

            byte[] imageData = file.getBytes();
            Set<ImageFilter> imageFilters = filters != null ? filters.stream()
                    .map(f -> ImageFilter.valueOf(f.toUpperCase())).collect(Collectors.toSet()) : new HashSet<>();
            if (imageFormat != null || targetWidth != null || targetHeight != null || !imageFilters.isEmpty()) {
                ImageProcessingOptions options = ImageProcessingOptions.builder()
                        .imageFormat(imageFormat)
                        .filters(imageFilters)
                        .width(targetWidth)
                        .height(targetHeight)
                        .build();
                imageData = imageProcessorService.processImage(new ByteArrayInputStream(file.getBytes()), options);

                if (imageFormat != null) {
                    extension = "." + imageFormat.name().toLowerCase();
                    storedFilename = storedFilename.replace(storedFilename.substring(storedFilename.lastIndexOf('.')),
                            extension);
                }
            }

            String contentType = imageFormat != null ? "image/" + imageFormat.name().toLowerCase() : file.getContentType();
            storageService.store(imageData, storedFilename, contentType, storageType);

            String thumbnailFilename = "thumb_" + storedFilename;
            byte[] thumbnailData = imageProcessorService.createThumbnail(new ByteArrayInputStream(imageData),
                    200, 200);
            storageService.store(thumbnailData, thumbnailFilename, "image/jpeg", storageType);

            int[] dimensions = imageProcessorService.getImageDimensions(imageData);

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

            user.recordUpload(imageData.length);
            userRepository.save(user);

            actionLogService.logActionWithTargetPhoto(user, ActionType.PHOTO_UPLOAD, "Uploaded photo: " + originalFilename,
                    ipUtils.getClientIp(request), photo.getId());
            log.info("Photo uploaded successfully: {} by {}", storedFilename, user.getUsername());
            return ResponseEntity.ok(photoMapper.toDto(photo));
        } catch (IOException e) {
            log.error("Error uploading photo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading photo: " + e.getMessage());
        }
    }

    @GetMapping("/browse")
    public ResponseEntity<Page<PhotoResponse>> browsePhotos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        Page<Photo> photos = photoRepository.findAllByOrderByUploadedAtDesc(pageable);

        actionLogService.logAnonymousAction(ActionType.PHOTO_VIEW, "Browsed photos paged " + page, ipUtils.getClientIp(request));
        return ResponseEntity.ok(photos.map(photoMapper::toDto));
    }

    @PostMapping("/search")
    public ResponseEntity<Page<PhotoResponse>> searchPhotos(@RequestBody PhotoSearchRequest searchRequest,
                                                            HttpServletRequest request) {
        Pageable pageable = PageRequest.of(
                searchRequest.getPage() != null ? searchRequest.getPage() : 0,
                searchRequest.getPageSize() != null ? searchRequest.getPageSize() : 10,
                Sort.by(Sort.Direction.DESC, "uploadedAt"));
        Page<Photo> photos = photoRepository.findAll(PhotoSpecification.fromSearchRequest(searchRequest), pageable);

        actionLogService.logAnonymousAction(ActionType.USER_SEARCH, "Searched photos with filters", ipUtils.getClientIp(request));
        return ResponseEntity.ok(photos.map(photoMapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhotoResponse> getPhoto(@PathVariable Long id, HttpServletRequest request) {
        Optional<Photo> photoOpt = photoRepository.findById(id);
        if (photoOpt.isEmpty()) return ResponseEntity.notFound().build();
        Photo photo = photoOpt.get();
        photo.incrementViewCount();
        photoRepository.save(photo);

        actionLogService.logAnonymousAction(ActionType.PHOTO_VIEW, "Viewed photo: " + id, ipUtils.getClientIp(request));
        return ResponseEntity.ok(photoMapper.toDto(photo));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('REGISTERED', 'ADMIN')")
    public ResponseEntity<?> editPhoto(
            @PathVariable Long id,
            @RequestBody PhotoEditRequest editRequest,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
            ) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");

        Optional<Photo> photoOpt = photoRepository.findById(id);
        if (photoOpt.isEmpty()) return ResponseEntity.notFound().build();
        Photo photo = photoOpt.get();

        if (!photo.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only edit your own photos");
        }

        if (editRequest.getDescription() != null) photo.setDescription(editRequest.getDescription());

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

        actionLogService.logActionWithTargetPhoto(user, ActionType.PHOTO_EDIT, "Edited photo: " + id,
                ipUtils.getClientIp(request), id);
        return ResponseEntity.ok(photoMapper.toDto(photo));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('REGISTERED', 'ADMIN')")
    public ResponseEntity<?> deletePhoto(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        Optional<Photo> photoOpt = photoRepository.findById(id);
        if (photoOpt.isEmpty()) return ResponseEntity.notFound().build();
        Photo photo = photoOpt.get();

        if (!photo.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only delete your own photos");
        }

        try {
            storageService.delete(photo.getStoredFileName(), photo.getStorageType());

            String thumbnailFilename = "thumb_" + photo.getStoredFileName();
            try {
                storageService.delete(thumbnailFilename, photo.getStorageType());
            } catch (Exception e) {
                log.warn("Could not delete thumbnail: {}", thumbnailFilename);
            }

            photoRepository.delete(photo);

            actionLogService.logActionWithTargetPhoto(user, ActionType.PHOTO_DELETE, "Deleted photo: " + id,
                    ipUtils.getClientIp(request), id);
            return ResponseEntity.ok().body("Photo deleted successfully");
        } catch (IOException e) {
            log.error("Error deleting photo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting photo: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadPhoto(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean original,
            @RequestParam(required = false) ImageFormat format,
            @RequestParam(required = false) Integer width,
            @RequestParam(required = false) Integer height,
            @RequestParam(required = false) Set<String> filters,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
            ) {
        Optional<Photo> photoOpt = photoRepository.findById(id);
        if (photoOpt.isEmpty()) return ResponseEntity.notFound().build();
        Photo photo = photoOpt.get();

        try {
            InputStream inputStream = storageService.retrieve(photo.getStoredFileName(), photo.getStorageType());
            byte[] imageData;

            if (!original && (format != null || width != null || height != null || (filters != null && !filters.isEmpty()))) {
                Set<ImageFilter> imageFilters = filters != null
                        ? filters.stream().map(f -> ImageFilter.valueOf(f.toUpperCase())).collect(Collectors.toSet())
                        : new HashSet<>();
                ImageProcessingOptions options = ImageProcessingOptions.builder()
                        .imageFormat(format)
                        .width(width)
                        .height(height)
                        .filters(imageFilters)
                        .build();
                imageData = imageProcessorService.processImage(inputStream, options);
            } else imageData = inputStream.readAllBytes();

            photo.incrementDownloadCount();
            photoRepository.save(photo);

            User user = userDetails != null ? userRepository.findByUsername(userDetails.getUsername()) : null;
            if (user != null) {
                actionLogService.logActionWithTargetPhoto(user, ActionType.PHOTO_DOWNLOAD,
                        "Downloaded photo: " + id + (original ? " (original)" : " (processed)"),
                        ipUtils.getClientIp(request), id);
            } else {
                actionLogService.logAnonymousAction(ActionType.PHOTO_DOWNLOAD, "Downloaded photo: " + id,
                        ipUtils.getClientIp(request));
            }

            String contentType = format != null ? "image/" + format.name().toLowerCase() : photo.getMimeType();
            String filename = format != null ? photo.getOriginalFileName().replace(
                    photo.getOriginalFileName().substring(photo.getOriginalFileName().lastIndexOf(".")),
                    "." + format.name().toLowerCase()
            ) : photo.getOriginalFileName();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + filename + "\"")
                    .body(new InputStreamResource(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            log.error("Error downloading photo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PhotoResponse>> getPhotosByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        Page<Photo> photos = photoRepository.findByUserOrderByUploadedAtDesc(userOpt.get(), pageable);

        return ResponseEntity.ok(photos.map(photoMapper::toDto));
    }

    @GetMapping("/file/{filename}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Optional<Photo> photoOptional = photoRepository.findByStoredFileName(filename);
            if (photoOptional.isEmpty()) {
                String originalFilename = filename.startsWith("thumb_") ? filename.substring(6) : filename;
                if (originalFilename != null) photoOptional = photoRepository.findByStoredFileName(originalFilename);
            }
            StorageType storageType = photoOptional.map(Photo::getStorageType).orElse(StorageType.LOCAL);
            InputStream inputStream = storageService.retrieve(filename, storageType);
            String contentType = photoOptional.map(Photo::getMimeType).orElse("application/octet-stream");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .body(new InputStreamResource(inputStream));
        } catch (IOException e) {
            log.error("Error downloading file", e);
            return ResponseEntity.notFound().build();
        }

    }

//    private PhotoResponse toDto(Photo photo) {
//        return PhotoResponse.builder()
//                .id(photo.getId())
//                .originalFileName(photo.getOriginalFileName())
//                .description(photo.getDescription())
//                .hashtags(photo.getHashtags().stream()
//                        .map(Hashtag::getTag)
//                        .collect(Collectors.toSet()))
//                .thumbnailUrl(photo.getThumbnailUrl())
//                .imageUrl(photo.getStorageUrl())
//                .processedUrl(photo.getProcessedUrl())
//                .author(photo.getUser().getUsername())
//                .authorId(photo.getUser().getId())
//                .fileSizeBytes(photo.getFileSizeBytes())
//                .width(photo.getWidth())
//                .height(photo.getHeight())
//                .uploadedAt(photo.getUploadedAt())
//                .editedAt(photo.getEditedAt())
//                .downloadCount(photo.getDownloadCount())
//                .viewCount(photo.getViewCount())
//                .build();
//    }
}
