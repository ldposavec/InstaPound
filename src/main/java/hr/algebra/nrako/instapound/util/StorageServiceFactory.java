package hr.algebra.nrako.instapound.util;

import hr.algebra.nrako.instapound.domain.enums.StorageType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory Pattern (Design Pattern #5)
 * Factory for creating appropriate storage service based on storage type
 * Adheres to Dependency Inversion Principle (SOLID) - depends on StorageService abstraction
 */
@Component
public class StorageServiceFactory {
    
    private final Map<StorageType, StorageService> storageServices = new HashMap<>();
    
    public StorageServiceFactory(List<StorageService> services) {
        for (StorageService service : services) {
            storageServices.put(service.getStorageType(), service);
        }
    }
    
    /**
     * Get storage service for the specified storage type
     * 
     * @param storageType Type of storage
     * @return Appropriate StorageService implementation
     * @throws IllegalArgumentException if storage type is not supported
     */
    public StorageService getStorageService(StorageType storageType) {
        StorageService service = storageServices.get(storageType);
        if (service == null) {
            throw new IllegalArgumentException("Unsupported storage type: " + storageType);
        }
        return service;
    }
}
