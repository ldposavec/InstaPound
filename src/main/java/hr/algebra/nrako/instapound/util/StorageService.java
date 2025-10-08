package hr.algebra.nrako.instapound.util;

import hr.algebra.nrako.instapound.domain.enums.StorageType;

import java.io.IOException;

/**
 * Strategy Pattern (Design Pattern #4) - Interface for Storage
 * Interface for different storage strategies (local, cloud)
 * Adheres to Interface Segregation Principle (SOLID)
 */
public interface StorageService {
    
    /**
     * Store a file
     * 
     * @param fileName File name
     * @param data File data
     * @return URL or path to the stored file
     * @throws IOException if storage fails
     */
    String store(String fileName, byte[] data) throws IOException;
    
    /**
     * Retrieve a file
     * 
     * @param fileIdentifier File identifier (path or URL)
     * @return File data
     * @throws IOException if retrieval fails
     */
    byte[] retrieve(String fileIdentifier) throws IOException;
    
    /**
     * Delete a file
     * 
     * @param fileIdentifier File identifier (path or URL)
     * @throws IOException if deletion fails
     */
    void delete(String fileIdentifier) throws IOException;
    
    /**
     * Get the storage type this service handles
     * 
     * @return StorageType
     */
    StorageType getStorageType();
}
