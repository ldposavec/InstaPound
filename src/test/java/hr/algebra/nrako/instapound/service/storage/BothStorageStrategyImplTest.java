package hr.algebra.nrako.instapound.service.storage;

import hr.algebra.nrako.instapound.enums.StorageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BothStorageStrategyImplTest {

    @Mock
    private LocalStorageStrategyImpl localStrategy;

    @Mock
    private CloudinaryStorageStrategyImpl cloudinaryStrategy;

    private BothStorageStrategyImpl bothStrategy;

    @BeforeEach
    void setUp() {
        bothStrategy = new BothStorageStrategyImpl(localStrategy, cloudinaryStrategy);
    }

    @Test
    void getStorageType_shouldReturnBoth() {
        assertEquals(StorageType.BOTH, bothStrategy.getStorageType());
    }

    @Test
    void storeMultipartFile_shouldStoreInBothLocalAndCloudinary() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test content".getBytes());
        when(cloudinaryStrategy.store(file, "test.jpg")).thenReturn("test.jpg");

        String result = bothStrategy.store(file, "test.jpg");

        assertEquals("test.jpg", result);
        verify(localStrategy).store(file, "test.jpg");
        verify(cloudinaryStrategy).store(file, "test.jpg");
    }

    @Test
    void storeMultipartFile_shouldReturnCloudinaryResult() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test content".getBytes());
        when(cloudinaryStrategy.store(file, "test.jpg")).thenReturn("test.jpg");

        String result = bothStrategy.store(file, "test.jpg");

        assertEquals("test.jpg", result);
    }

    @Test
    void storeBytes_shouldStoreInBothLocalAndCloudinary() throws IOException {
        byte[] data = "image-data".getBytes();
        when(cloudinaryStrategy.store(data, "image.jpg", "image/jpeg")).thenReturn("image.jpg");

        String result = bothStrategy.store(data, "image.jpg", "image/jpeg");

        assertEquals("image.jpg", result);
        verify(localStrategy).store(data, "image.jpg", "image/jpeg");
        verify(cloudinaryStrategy).store(data, "image.jpg", "image/jpeg");
    }

    @Test
    void retrieve_shouldRetrieveFromLocalWhenFileExistsLocally() throws IOException {
        InputStream localStream = new ByteArrayInputStream("local content".getBytes());
        when(localStrategy.exists("test.jpg")).thenReturn(true);
        when(localStrategy.retrieve("test.jpg")).thenReturn(localStream);

        InputStream result = bothStrategy.retrieve("test.jpg");

        assertEquals(localStream, result);
        verify(cloudinaryStrategy, never()).retrieve(anyString());
    }

    @Test
    void retrieve_shouldFallbackToCloudinaryWhenNotLocal() throws IOException {
        InputStream cloudStream = new ByteArrayInputStream("cloud content".getBytes());
        when(localStrategy.exists("test.jpg")).thenReturn(false);
        when(cloudinaryStrategy.retrieve("test.jpg")).thenReturn(cloudStream);

        InputStream result = bothStrategy.retrieve("test.jpg");

        assertEquals(cloudStream, result);
    }

    @Test
    void retrieve_shouldCheckLocalFirst() throws IOException {
        when(localStrategy.exists("test.jpg")).thenReturn(true);
        when(localStrategy.retrieve("test.jpg")).thenReturn(new ByteArrayInputStream("local content".getBytes()));

        bothStrategy.retrieve("test.jpg");

        verify(localStrategy).exists("test.jpg");
        verify(localStrategy).retrieve("test.jpg");
    }

    @Test
    void delete_shouldDeleteFromBothStrategies() throws IOException {
        bothStrategy.delete("test.jpg");

        verify(localStrategy).delete("test.jpg");
        verify(cloudinaryStrategy).delete("test.jpg");
    }

    @Test
    void delete_shouldNotThrowWhenCloudinaryDeleteFails() throws IOException {
        doThrow(new IOException("Cloudinary error")).when(cloudinaryStrategy).delete("test.jpg");

        assertDoesNotThrow(() -> bothStrategy.delete("test.jpg"));

        verify(localStrategy).delete("test.jpg");
    }

    @Test
    void delete_shouldStillDeleteLocalEvenIfCloudinaryDeleteFails() throws IOException {
        doThrow(new IOException("Cloudinary error")).when(cloudinaryStrategy).delete("test.jpg");

        bothStrategy.delete("test.jpg");

        verify(localStrategy).delete("test.jpg");
    }

    @Test
    void exists_shouldReturnTrueWhenExistsLocally() {
        when(localStrategy.exists("test.jpg")).thenReturn(true);

        assertTrue(bothStrategy.exists("test.jpg"));
    }

    @Test
    void exists_shouldReturnTrueWhenExistsOnCloudinaryOnly() {
        when(localStrategy.exists("test.jpg")).thenReturn(false);
        when(cloudinaryStrategy.exists("test.jpg")).thenReturn(true);

        assertTrue(bothStrategy.exists("test.jpg"));
    }

    @Test
    void exists_shouldReturnFalseWhenNotInEither() {
        when(localStrategy.exists("test.jpg")).thenReturn(false);
        when(cloudinaryStrategy.exists("test.jpg")).thenReturn(false);

        assertFalse(bothStrategy.exists("test.jpg"));
    }

    @Test
    void getUrl_shouldDelegateToCloudinary() {
        when(cloudinaryStrategy.getUrl("test.jpg")).thenReturn("http://cloudinary.com/test.jpg");

        String result = bothStrategy.getUrl("test.jpg");

        assertEquals("http://cloudinary.com/test.jpg", result);
        verify(cloudinaryStrategy).getUrl("test.jpg");
    }
}
