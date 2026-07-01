package hr.algebra.nrako.instapound.controller;

import hr.algebra.nrako.instapound.model.dto.response.PhotoResponse;
import hr.algebra.nrako.instapound.model.entity.Photo;
import hr.algebra.nrako.instapound.model.serialization.PhotoSnapshot;
import hr.algebra.nrako.instapound.repository.PhotoRepository;
import hr.algebra.nrako.instapound.service.implementations.PhotoSerializationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@RestController
@RequestMapping("/api/photos/serialization")
@RequiredArgsConstructor
public class PhotoSerializationController {
    private final PhotoRepository photoRepository;
    private final PhotoSerializationService photoSerializationService;

    @PostMapping(value = "/{id}/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> exportSnapshot(@PathVariable Long id) throws IOException {
        Optional<Photo> photoOpt = photoRepository.findById(id);
        if (photoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Photo photo = photoOpt.get();
        byte[] serialized = photoSerializationService.serialize(new PhotoSnapshot(
                photo.getId(),
                photo.getUser() != null ? photo.getUser().getId() : null,
                photo.getUser() != null ? photo.getUser().getUsername() : null,
                photo.getOriginalFileName(),
                photo.getDescription(),
                photo.getHashtags() == null ? null : photo.getHashtags().stream().map(h -> h.getTag()).collect(java.util.stream.Collectors.toSet()),
                photo.getStorageType(),
                photo.getStorageUrl(),
                photo.getThumbnailUrl(),
                photo.getProcessedUrl(),
                photo.getFileSizeBytes()
        ));
        return ResponseEntity.ok(serialized);
    }

    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<PhotoResponse> importSnapshot(@RequestBody byte[] fileBytes) throws IOException, ClassNotFoundException {
        PhotoSnapshot snapshot = photoSerializationService.deserializePhotoSnapshot(fileBytes);
        return ResponseEntity.ok(snapshot.toResponse());
    }

    @PostMapping(value = "/import-file", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<PhotoResponse> importSnapshotFromPath(@RequestBody String filePath) throws IOException, ClassNotFoundException {
        PhotoSnapshot snapshot = photoSerializationService.deserializePhotoSnapshot(Path.of(filePath.trim()));
        return ResponseEntity.ok(snapshot.toResponse());
    }
}




