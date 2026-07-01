package hr.algebra.nrako.instapound.service.storage;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.Url;
import com.cloudinary.api.ApiResponse;
import hr.algebra.nrako.instapound.config.StorageConfig;
import hr.algebra.nrako.instapound.enums.StorageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CloudinaryStorageStrategyImplTest {

    @Mock
    private StorageConfig storageConfig;

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private Api api;

    @Mock
    private Url url;

    private StorageConfig.CloudinaryConfig cloudinaryConfig;
    private CloudinaryStorageStrategyImpl strategy;

    @BeforeEach
    public void setUp() {
        cloudinaryConfig = new StorageConfig.CloudinaryConfig();
        cloudinaryConfig.setCloudName("demo");
        cloudinaryConfig.setApiKey("key");
        cloudinaryConfig.setApiSecret("secret");
        cloudinaryConfig.setFolder("instapound");
        lenient().when(storageConfig.getCloudinary()).thenReturn(cloudinaryConfig);

        strategy = new CloudinaryStorageStrategyImpl(storageConfig);
    }

    private void injectCloudinary(Cloudinary value) throws NoSuchFieldException, IllegalAccessException {
        Field field = CloudinaryStorageStrategyImpl.class.getDeclaredField("cloudinary");
        field.setAccessible(true);
        field.set(strategy, value);
    }

    @Test
    void getStorageType_shouldReturnCloudinary() {
        assertThat(strategy.getStorageType()).isEqualTo(StorageType.CLOUDINARY);
    }

    @Test
    void storeBytes_shouldReturnFilenameOnSuccess() throws Exception {
        injectCloudinary(cloudinary);
        when(cloudinary.uploader()).thenReturn(uploader);
        Map<String, Object> result = new HashMap<>();
        result.put("secure_url", "https://res.cloudinary.com/demo/image/upload/instapound/a.jpg");
        when(uploader.upload(any(byte[].class), any())).thenReturn(result);

        String returned = strategy.store(new byte[]{1, 2, 3}, "a.jpg", "image/jpeg");

        assertThat(returned).isEqualTo("a.jpg");
        verify(uploader).upload(any(byte[].class), any());
    }

    @Test
    void storeBytes_shouldOverwriteExistingFile() throws Exception {
        injectCloudinary(cloudinary);
        when(cloudinary.uploader()).thenReturn(uploader);
        Map<String, Object> result = new HashMap<>();
        result.put("secure_url", "https://res.cloudinary.com/demo/image/upload/instapound/a.jpg");
        when(uploader.upload(any(byte[].class), any())).thenReturn(result);

        strategy.store(new byte[]{9}, "a.jpg", "image/jpeg");

        org.mockito.ArgumentCaptor<Map<String, Object>> optionsCaptor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(any(byte[].class), optionsCaptor.capture());
        assertThat(optionsCaptor.getValue().get("public_id")).isEqualTo("a");
        assertThat(optionsCaptor.getValue().get("folder")).isEqualTo("instapound");
    }

    @Test
    void storeBytes_shouldThrowIOExceptionWhenCloudinaryNotConfigured() throws Exception {
        injectCloudinary(null);

        assertThatThrownBy(() -> strategy.store(new byte[]{1}, "a.jpg", "image/jpeg"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Cloudinary is not configured");
    }

    @Test
    void storeBytes_shouldWrapUploaderExceptionInIOException() throws Exception {
        injectCloudinary(cloudinary);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any())).thenThrow(new RuntimeException("upload boom"));

        assertThatThrownBy(() -> strategy.store(new byte[]{1}, "a.jpg", "image/jpeg"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Failed to uploading to Cloudinary");
    }

    @Test
    void storeMultipartFile_shouldDelegateToByteStoreAndReturnFilename() throws Exception {
        injectCloudinary(cloudinary);
        when(cloudinary.uploader()).thenReturn(uploader);
        Map<String, Object> result = new HashMap<>();
        result.put("secure_url", "https://res.cloudinary.com/demo/image/upload/instapound/a.jpg");
        when(uploader.upload(any(byte[].class), any())).thenReturn(result);
        MultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", "bytes".getBytes());

        String returned = strategy.store(file, "a.jpg");

        assertThat(returned).isEqualTo("a.jpg");
        verify(uploader).upload(any(byte[].class), any());
    }

    @Test
    void storeMultipartFile_shouldOverwriteExistingFile() throws Exception {
        injectCloudinary(cloudinary);
        when(cloudinary.uploader()).thenReturn(uploader);
        Map<String, Object> result = new HashMap<>();
        result.put("secure_url", "https://res.cloudinary.com/demo/image/upload/instapound/photo.png");
        when(uploader.upload(any(byte[].class), any())).thenReturn(result);
        MultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "bytes".getBytes());

        strategy.store(file, "photo.png");

        org.mockito.ArgumentCaptor<Map<String, Object>> optionsCaptor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(any(byte[].class), optionsCaptor.capture());
        assertThat(optionsCaptor.getValue().get("public_id")).isEqualTo("photo");
    }

    @Test
    void storeMultipartFile_shouldThrowIOExceptionWhenNotConfigured() throws Exception {
        injectCloudinary(null);
        MultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", "bytes".getBytes());

        assertThatThrownBy(() -> strategy.store(file, "a.jpg"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Cloudinary is not configured");
    }

    @Test
    void retrieve_shouldThrowIOExceptionWhenNotConfigured() throws Exception {
        injectCloudinary(null);

        assertThatThrownBy(() -> strategy.retrieve("a.jpg"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Cloudinary is not configured");
    }

    @Test
    void retrieve_shouldThrowIOExceptionForNonCloudinaryHost() throws Exception {
        injectCloudinary(cloudinary);
        when(cloudinary.url()).thenReturn(url);
        when(url.secure(true)).thenReturn(url);
        when(url.resourceType("image")).thenReturn(url);
        when(url.generate(any())).thenReturn("https://evil.example.com/instapound/a");

        assertThatThrownBy(() -> strategy.retrieve("a.jpg"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Failed to retrieve file from Cloudinary");
    }

    @Test
    void delete_shouldCallDestroyWithFullPublicId() throws Exception {
        injectCloudinary(cloudinary);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(any(), any())).thenReturn(new HashMap<>());

        strategy.delete("a.jpg");

        verify(uploader).destroy(eq("instapound/a"), any());
    }

    @Test
    void delete_shouldThrowIOExceptionWhenNotConfigured() throws Exception {
        injectCloudinary(null);

        assertThatThrownBy(() -> strategy.delete("a.jpg"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Cloudinary is not configured");
    }

    @Test
    void delete_shouldWrapDestroyExceptionInIOException() throws Exception {
        injectCloudinary(cloudinary);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(any(), any())).thenThrow(new RuntimeException("destroy boom"));

        assertThatThrownBy(() -> strategy.delete("a.jpg"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Failed to delete file from Cloudinary");
    }

    @Test
    void exists_shouldReturnFalseWhenNotConfigured() throws Exception {
        injectCloudinary(null);

        assertThat(strategy.exists("a.jpg")).isFalse();
    }

    @Test
    void exists_shouldReturnFalseWhenApiThrows() throws Exception {
        injectCloudinary(cloudinary);
        when(cloudinary.api()).thenReturn(api);
        when(api.resource(any(), any())).thenThrow(new RuntimeException("not found"));

        assertThat(strategy.exists("a.jpg")).isFalse();
    }

    @Test
    void getUrl_shouldReturnNullWhenNotConfigured() throws Exception {
        injectCloudinary(null);

        assertThat(strategy.getUrl("a.jpg")).isNull();
    }

    @Test
    void getUrl_shouldGenerateSecureUrlWithFullPublicId() throws Exception {
        injectCloudinary(cloudinary);
        when(cloudinary.url()).thenReturn(url);
        when(url.secure(true)).thenReturn(url);
        when(url.resourceType("image")).thenReturn(url);
        when(url.generate("instapound/a"))
                .thenReturn("https://res.cloudinary.com/demo/image/upload/instapound/a");

        String result = strategy.getUrl("a.jpg");

        assertThat(result).isEqualTo("https://res.cloudinary.com/demo/image/upload/instapound/a");
        verify(url).generate("instapound/a");
    }
}
