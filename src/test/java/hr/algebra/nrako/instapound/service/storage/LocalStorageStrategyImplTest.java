package hr.algebra.nrako.instapound.service.storage;

import hr.algebra.nrako.instapound.config.StorageConfig;
import hr.algebra.nrako.instapound.enums.StorageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class LocalStorageStrategyImplTest {

    @TempDir
    Path tempDir;

    private LocalStorageStrategyImpl storageStrategy;

    @BeforeEach
    public void setUp() {
        StorageConfig config = new StorageConfig();
        config.setLocalPath(tempDir.resolve("uploads").toString());
        config.setThumbnailPath(tempDir.resolve("uploads/thumbnail").toString());

        storageStrategy = new LocalStorageStrategyImpl(config);
        storageStrategy.init();
    }

    @Test
    void getStorageType_shouldReturnLocal() {
        assertEquals(StorageType.LOCAL, storageStrategy.getStorageType());
    }

    @Test
    void storeMultipartFile_shouldWriteFileToDisk() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test content".getBytes());

        String result = storageStrategy.store(file, "test.jpg");

        assertEquals("test.jpg", result);
        assertTrue(Files.exists(storageStrategy.getRootLocation().resolve("test.jpg")));
    }

    @Test
    void storeMultipartFile_shouldRejectPathTraversal() {
        MockMultipartFile file = new MockMultipartFile("file", "evil.jpg", "image/jpeg",
                "malicious content".getBytes());

        assertThrows(IOException.class, () -> storageStrategy.store(file, "../evil.jpg"));
    }

    @Test
    void storeMultipartFile_shouldOverwriteExistingFile() throws IOException {
        MockMultipartFile file1 = new MockMultipartFile("file", "test.jpg", "image/jpeg", "v1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "test.jpg", "image/jpeg", "v2".getBytes());

        storageStrategy.store(file1, "test.jpg");
        storageStrategy.store(file2, "test.jpg");

        byte[] content = Files.readAllBytes(storageStrategy.getRootLocation().resolve("test.jpg"));
        assertEquals("v2", new String(content));
    }

    @Test
    void storeBytes_shouldWriteToDisk() throws IOException {
        byte[] data = "image-data".getBytes();

        String result = storageStrategy.store(data, "image.jpg", "image/jpeg");

        assertEquals("image.jpg", result);
        byte[] stored = Files.readAllBytes(storageStrategy.getRootLocation().resolve("image.jpg"));
        assertArrayEquals(data, stored);
    }

    @Test
    void storeBytes_shouldRejectPathTraversal() {
        byte[] data = "image-data".getBytes();
        assertThrows(IOException.class, () -> storageStrategy.store(data, "../evil.jpg", "image/jpeg"));
    }

    @Test
    void retrieve_shouldReturnInputStreamForExistingFile() throws IOException {
        byte[] data = "image-data".getBytes();
        storageStrategy.store(data, "image.jpg", "image/jpeg");

        InputStream result = storageStrategy.retrieve("image.jpg");

        assertNotNull(result);
        assertArrayEquals(data, result.readAllBytes());
        result.close();
    }

    @Test
    void retrieve_shouldThrowWhenFileNotFound() {
        assertThrows(FileNotFoundException.class, () -> storageStrategy.retrieve("nonexistent.jpg"));
    }

    @Test
    void retrieve_shouldFallbackToThumbnailDirectory() throws IOException {
        Path thumbFile = storageStrategy.getThumbnailLocation().resolve("thumb.jpg");
        Files.write(thumbFile, "image-data".getBytes());

        InputStream result = storageStrategy.retrieve("thumb.jpg");

        assertNotNull(result);
        assertArrayEquals("image-data".getBytes(), result.readAllBytes());
        result.close();
    }

    @Test
    void delete_shouldRemoveFileFromDisk()  throws IOException {
        storageStrategy.store("image-data".getBytes(), "image.jpg", "image/jpeg");
        assertTrue(Files.exists(storageStrategy.getRootLocation().resolve("image.jpg")));

        storageStrategy.delete("image.jpg");

        assertFalse(Files.exists(storageStrategy.getRootLocation().resolve("image.jpg")));
    }

    @Test
    void delete_shouldNotThrowWhenFileDoesNotExist() {
        assertDoesNotThrow(() -> storageStrategy.delete("nonexisting.jpg"));
    }

    @Test
    void delete_shouldAlsoDeleteFromThumbnailDirectory() throws IOException {
        storageStrategy.store("image-data".getBytes(), "image.jpg", "image/jpeg");
        Path thumbFile = storageStrategy.getThumbnailLocation().resolve("image.jpg");
        Files.write(thumbFile, "image-data".getBytes());

        storageStrategy.delete("image.jpg");

        assertFalse(Files.exists(thumbFile));
    }

    @Test
    void exists_shouldReturnTrueWhenFileExists() throws IOException {
        storageStrategy.store("image-data".getBytes(), "image.jpg", "image/jpeg");
        assertTrue(storageStrategy.exists("image.jpg"));
    }

    @Test
    void exists_shouldReturnFalseWhenFileDoesNotExist() {
        assertFalse(storageStrategy.exists("nonexisting.jpg"));
    }

    @Test
    void getUrl_shouldReturnApiPath() {
        String url = storageStrategy.getUrl("image.jpg");
        assertEquals("/api/photos/file/image.jpg", url);
    }

    @Test
    void getUrl_shouldIncludeFilename() {
        String url = storageStrategy.getUrl("image.jpg");
        assertTrue(url.contains("image.jpg"));
    }
}
