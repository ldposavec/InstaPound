package hr.algebra.nrako.instapound.util;

import hr.algebra.nrako.instapound.domain.enums.StorageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Strategy Pattern (Design Pattern #4) - Concrete Implementation for Local Storage
 * Local filesystem storage implementation
 * Adheres to Open/Closed Principle (SOLID)
 */
@Service
public class LocalStorageService implements StorageService {
    
    @Value("${app.storage.local.path:uploads}")
    private String storagePath;
    
    @Override
    public String store(String fileName, byte[] data) throws IOException {
        Path uploadPath = Paths.get(storagePath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        
        return filePath.toString();
    }
    
    @Override
    public byte[] retrieve(String fileIdentifier) throws IOException {
        Path filePath = Paths.get(fileIdentifier);
        return Files.readAllBytes(filePath);
    }
    
    @Override
    public void delete(String fileIdentifier) throws IOException {
        Path filePath = Paths.get(fileIdentifier);
        Files.deleteIfExists(filePath);
    }
    
    @Override
    public StorageType getStorageType() {
        return StorageType.LOCAL;
    }
}
