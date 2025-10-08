package hr.algebra.nrako.instapound.controller;

import hr.algebra.nrako.instapound.dto.PhotoResponse;
import hr.algebra.nrako.instapound.dto.PhotoSearchRequest;
import hr.algebra.nrako.instapound.dto.PhotoUploadRequest;
import hr.algebra.nrako.instapound.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

/**
 * REST Controller for photo operations
 * Adheres to Single Responsibility Principle (SOLID)
 */
@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
public class PhotoController {
    
    private final PhotoService photoService;
    
    /**
     * Upload a new photo
     * 
     * @param file Image file
     * @param request Upload metadata
     * @param userId User ID (from security context)
     * @return Created photo response
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoResponse> uploadPhoto(
            @RequestPart("file") MultipartFile file,
            @RequestPart("request") PhotoUploadRequest request,
            @RequestHeader("X-User-Id") Long userId) throws IOException {
        PhotoResponse response = photoService.uploadPhoto(file, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get a photo by ID
     * 
     * @param id Photo ID
     * @return Photo response
     */
    @GetMapping("/{id}")
    public ResponseEntity<PhotoResponse> getPhoto(@PathVariable Long id) {
        PhotoResponse response = photoService.getPhoto(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get latest photos (default view)
     * 
     * @param page Page number (default 0)
     * @param size Page size (default 10)
     * @return Page of photo responses
     */
    @GetMapping
    public ResponseEntity<Page<PhotoResponse>> getLatestPhotos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PhotoResponse> photos = photoService.getLatestPhotos(page, size);
        return ResponseEntity.ok(photos);
    }
    
    /**
     * Search photos based on criteria
     * 
     * @param searchRequest Search criteria
     * @return Page of photo responses
     */
    @PostMapping("/search")
    public ResponseEntity<Page<PhotoResponse>> searchPhotos(@RequestBody PhotoSearchRequest searchRequest) {
        Page<PhotoResponse> photos = photoService.searchPhotos(searchRequest);
        return ResponseEntity.ok(photos);
    }
    
    /**
     * Update photo metadata
     * 
     * @param id Photo ID
     * @param description New description
     * @param hashtags New hashtags
     * @param userId User ID (from security context)
     * @return Updated photo response
     */
    @PutMapping("/{id}")
    public ResponseEntity<PhotoResponse> updatePhoto(
            @PathVariable Long id,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Set<String> hashtags,
            @RequestHeader("X-User-Id") Long userId) {
        PhotoResponse response = photoService.updatePhoto(id, description, hashtags, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a photo
     * 
     * @param id Photo ID
     * @param userId User ID (from security context)
     * @return No content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) throws IOException {
        photoService.deletePhoto(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Download a photo
     * 
     * @param id Photo ID
     * @param processed Whether to download processed version (default false)
     * @param userId User ID (optional, can be null for anonymous)
     * @return Photo data
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadPhoto(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean processed,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) throws IOException {
        byte[] photoData = photoService.downloadPhoto(id, processed, userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentDispositionFormData("attachment", "photo_" + id + ".jpg");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(photoData);
    }
}
