package hr.algebra.nrako.instapound.service.implementations;

import hr.algebra.nrako.instapound.enums.StorageType;
import hr.algebra.nrako.instapound.model.entity.Hashtag;
import hr.algebra.nrako.instapound.model.entity.Photo;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.serialization.PhotoSnapshot;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhotoSerializationServiceTest {

    private final PhotoSerializationService service = new PhotoSerializationService();

    @Test
    void serializeAndDeserializeWhitelistedPhotoSnapshot_shouldWork() throws Exception {
        Photo photo = buildPhoto();
        byte[] serialized = service.serialize(PhotoSnapshot.fromPhoto(photo));

        PhotoSnapshot snapshot = service.deserializePhotoSnapshot(serialized);

        assertEquals(10L, snapshot.getPhotoId());
        assertEquals(100L, snapshot.getAuthorId());
        assertEquals("owner", snapshot.getAuthorUsername());
        assertEquals("photo.jpg", snapshot.getOriginalFileName());
        assertEquals("desc", snapshot.getDescription());
        assertEquals(1, snapshot.getHashtags().size());
        assertEquals(StorageType.LOCAL, snapshot.getStorageType());
    }

    @Test
    void deserialize_shouldRejectInvalidMagicBytesBeforeReadingObject() {
        byte[] invalid = new byte[]{'N', 'O', 'T', '!'};
        assertThrows(IOException.class, () -> service.deserializePhotoSnapshot(invalid));
    }

    @Test
    void deserialize_shouldRejectNonWhitelistedClass() throws Exception {
        byte[] bytes = serializeNonWhitelistedObject();
        assertThrows(InvalidClassException.class, () -> service.deserializePhotoSnapshot(bytes));
    }

    @Test
    void deserializeFromFile_shouldRejectInvalidHeaderFromTextFile() throws Exception {
        Path temp = Files.createTempFile("photo-serialization-invalid", ".txt");
        Files.writeString(temp, "plain text");
        assertThrows(IOException.class, () -> service.deserializePhotoSnapshot(temp));
    }

    private static Photo buildPhoto() {
        User user = User.builder().id(100L).username("owner").build();
        Hashtag hashtag = Hashtag.builder().withTag("test").build();
        return Photo.builder()
                .id(10L)
                .user(user)
                .originalFileName("photo.jpg")
                .description("desc")
                .hashtags(new HashSet<>(java.util.Set.of(hashtag)))
                .storageType(StorageType.LOCAL)
                .storageUrl("/files/photo.jpg")
                .thumbnailUrl("/files/thumb_photo.jpg")
                .processedUrl("/files/processed_photo.jpg")
                .fileSizeBytes(1234L)
                .build();
    }

    private static byte[] serializeNonWhitelistedObject() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(new TreeMap<>(java.util.Map.of("k", "v")));
        }
        return out.toByteArray();
    }
}



