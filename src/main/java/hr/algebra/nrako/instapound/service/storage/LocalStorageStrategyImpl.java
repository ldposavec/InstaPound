package hr.algebra.nrako.instapound.service.storage;

import hr.algebra.nrako.instapound.config.StorageConfig;
import hr.algebra.nrako.instapound.enums.StorageType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalStorageStrategyImpl implements StorageStrategy {
    private final StorageConfig storageConfig;
    private Path rootLocation;
    private Path thumbnailLocation;

    @PostConstruct
    public void init() {
        rootLocation = Paths.get(storageConfig.getLocalPath()).toAbsolutePath().normalize();
        thumbnailLocation = Paths.get(storageConfig.getThumbnailPath()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(rootLocation);
            Files.createDirectories(thumbnailLocation);
            log.info("Local storage initialized at: {}", rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.LOCAL;
    }

    @Override
    public String store(MultipartFile file, String filename) throws IOException {
        Path destinationFile = rootLocation.resolve(Paths.get(filename)).normalize().toAbsolutePath();

        if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
            throw new IOException("Cannot store file outside current directory.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }

        log.info("Stored file locally: {}", filename);
        return filename;
    }

    @Override
    public String store(byte[] data, String filename, String contentType) throws IOException {
        Path destinationFile = rootLocation.resolve(Paths.get(filename)).normalize().toAbsolutePath();

        if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
            throw new IOException("Cannot store file outside current directory.");
        }

        Files.write(destinationFile, data);
        log.info("Stored processed file locally: {}", filename);
        return filename;
    }

    @Override
    public InputStream retrieve(String filename) throws IOException {
        Path file = rootLocation.resolve(filename);
        if (!Files.exists(file)) file = thumbnailLocation.resolve(filename);
        if (!Files.exists(file)) throw new FileNotFoundException("File not found: " + filename);
        return new FileInputStream(file.toFile());
    }

    @Override
    public void delete(String filename) throws IOException {
        Path file = rootLocation.resolve(filename);
        Files.deleteIfExists(file);

        Path thumbnail = thumbnailLocation.resolve(filename);
        Files.deleteIfExists(thumbnail);

        log.info("Deleted file locally: {}", filename);
    }

    @Override
    public boolean exists(String filename) {
        Path file = rootLocation.resolve(filename);
        return Files.exists(file);
    }

    @Override
    public String getUrl(String filename) {
        return "/api/photos/file/" + filename;
    }

    public Path getRootLocation() {
        return rootLocation;
    }

    public Path getThumbnailLocation() {
        return thumbnailLocation;
    }
}
