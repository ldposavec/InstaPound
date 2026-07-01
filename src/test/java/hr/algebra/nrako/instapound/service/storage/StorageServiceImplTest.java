package hr.algebra.nrako.instapound.service.storage;

import hr.algebra.nrako.instapound.enums.StorageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StorageServiceImplTest {

    @Mock
    private StorageStrategy localStrategy;

    @Mock
    private StorageStrategy cloudinaryStrategy;

    private StorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        when(localStrategy.getStorageType()).thenReturn(StorageType.LOCAL);
        when(cloudinaryStrategy.getStorageType()).thenReturn(StorageType.CLOUDINARY);
        storageService = new StorageServiceImpl(List.of(localStrategy, cloudinaryStrategy));
    }

    @Test
    void getStrategy_shouldReturnLocalStrategyForLocalType() {
        StorageStrategy result = storageService.getStrategy(StorageType.LOCAL);
        assertEquals(localStrategy, result);
    }

    @Test
    void getStrategy_shouldReturnCloudinaryStrategyForCloudinaryType() {
        StorageStrategy result = storageService.getStrategy(StorageType.CLOUDINARY);
        assertEquals(cloudinaryStrategy, result);
    }

    @Test
    void getStrategy_shouldFallbackToLocalWhenTypeNotAvailable() {
        StorageStrategy result = storageService.getStrategy(StorageType.BOTH);
        assertEquals(localStrategy, result);
    }

    @Test
    void store_shouldDelegateToCorrectStrategy() throws IOException {
        byte[] data = "image-data".getBytes();
        when(localStrategy.store(data, "image.jpg", "image/jpeg")).thenReturn("file.jpg");

        String result = storageService.store(data, "image.jpg", "image/jpeg", StorageType.LOCAL);

        assertEquals("file.jpg", result);
        verify(localStrategy).store(data, "image.jpg", "image/jpeg");
    }

    @Test
    void store_shouldUseCloudinaryWhenSpecified() throws IOException {
        byte[] data = "image-data".getBytes();
        when(cloudinaryStrategy.store(data, "image.jpg", "image/jpeg")).thenReturn("file.jpg");

        String result = storageService.store(data, "image.jpg", "image/jpeg", StorageType.CLOUDINARY);

        assertEquals("file.jpg", result);
        verify(cloudinaryStrategy).store(data, "image.jpg", "image/jpeg");
    }

    @Test
    void store_shouldFallbackToLocalForUnknownType() throws IOException {
        byte[] data = "image-data".getBytes();
        when(localStrategy.store(data, "image.jpg", "image/jpeg")).thenReturn("file.jpg");

        String result = storageService.store(data, "image.jpg", "image/jpeg", StorageType.BOTH);

        assertEquals("file.jpg", result);
        verify(localStrategy).store(data, "image.jpg", "image/jpeg");
    }

    @Test
    void retrieve_shouldDelegateToCorrectStrategy() throws IOException {
        InputStream expected = new ByteArrayInputStream("image-data".getBytes());
        when(localStrategy.retrieve("image.jpg")).thenReturn(expected);

        InputStream result = storageService.retrieve("image.jpg", StorageType.LOCAL);

        assertEquals(expected, result);
        verify(localStrategy).retrieve("image.jpg");
    }

    @Test
    void retrieve_shouldUseCloudinaryWhenSpecified() throws IOException {
        InputStream expected = new ByteArrayInputStream("image-data".getBytes());
        when(cloudinaryStrategy.retrieve("image.jpg")).thenReturn(expected);

        InputStream result = storageService.retrieve("image.jpg", StorageType.CLOUDINARY);

        assertEquals(expected, result);
        verify(cloudinaryStrategy).retrieve("image.jpg");
    }

    @Test
    void delete_shouldDelegateToCorrectStrategy() throws IOException {
        storageService.delete("image.jpg", StorageType.LOCAL);
        verify(localStrategy).delete("image.jpg");
    }

    @Test
    void delete_shouldUseCloudinaryWhenSpecified() throws IOException {
        storageService.delete("image.jpg", StorageType.CLOUDINARY);
        verify(cloudinaryStrategy).delete("image.jpg");
    }

    @Test
    void getUrl_shouldDelegateToCorrectStrategy() {
        when(localStrategy.getUrl("image.jpg")).thenReturn("/api/photos/file/image.jpg");

        String result = storageService.getUrl("image.jpg", StorageType.LOCAL);

        assertEquals("/api/photos/file/image.jpg", result);
    }

    @Test
    void getUrl_shouldReturnCloudinaryUrlWhenSpecified() {
        when(cloudinaryStrategy.getUrl("image.jpg")).thenReturn("https://res.cloudinary.com/demo/image/image.jpg");

        String result = storageService.getUrl("image.jpg", StorageType.CLOUDINARY);

        assertEquals("https://res.cloudinary.com/demo/image/image.jpg", result);
    }
}
