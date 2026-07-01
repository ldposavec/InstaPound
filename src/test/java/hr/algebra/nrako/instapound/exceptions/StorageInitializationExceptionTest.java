package hr.algebra.nrako.instapound.exceptions;

import hr.algebra.nrako.instapound.config.StorageConfig;
import hr.algebra.nrako.instapound.service.storage.LocalStorageStrategyImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StorageInitializationExceptionTest {

    @Mock
    private StorageConfig storageConfig;

    @InjectMocks
    private LocalStorageStrategyImpl lss;

    @Test
    void initializingLocalStorage_shouldReturnThisExceptionOnError() {
        when(storageConfig.getLocalPath()).thenReturn("/invalid/path/to/storage");
        when(storageConfig.getThumbnailPath()).thenReturn("/invalid/path/to/thumbnails");

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.createDirectories(any(Path.class))).thenThrow(new IOException("Simulated " +
                    "disk error"));

            StorageInitializationException exception = assertThrows(StorageInitializationException.class,
                    () -> lss.init(), "Expected init() to throw StorageInitializationException");

            assertEquals("Could not initialize storage location", exception.getMessage());
            assertEquals(IOException.class, exception.getCause().getClass());
        }
    }
}
