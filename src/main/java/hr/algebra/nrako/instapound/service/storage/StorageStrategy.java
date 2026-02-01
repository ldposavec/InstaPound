package hr.algebra.nrako.instapound.service.storage;

import hr.algebra.nrako.instapound.enums.StorageType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Storage Strategy Interface - Strategy Pattern
 * Allows switching between different storage implementations (local, cloud)
 */
public interface StorageStrategy {
    
    /**
     * Get the storage type this strategy handles
     */
    StorageType getStorageType();
    
    /**
     * Store a file and return the storage URL/path
     */
    String store(MultipartFile file, String filename) throws IOException;
    
    /**
     * Store bytes and return the storage URL/path
     */
    String store(byte[] data, String filename, String contentType) throws IOException;
    
    /**
     * Retrieve a file as input stream
     */
    InputStream retrieve(String filename) throws IOException;
    
    /**
     * Delete a file
     */
    void delete(String filename) throws IOException;
    
    /**
     * Check if file exists
     */
    boolean exists(String filename);
    
    /**
     * Get the full URL for a stored file
     */
    String getUrl(String filename);
}
