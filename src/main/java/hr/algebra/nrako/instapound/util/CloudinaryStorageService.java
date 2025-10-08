package hr.algebra.nrako.instapound.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import hr.algebra.nrako.instapound.domain.enums.StorageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * Strategy Pattern (Design Pattern #4) - Concrete Implementation for Cloud Storage
 * Cloudinary cloud storage implementation
 * Adheres to Open/Closed Principle (SOLID)
 */
@Service
public class CloudinaryStorageService implements StorageService {
    
    private final Cloudinary cloudinary;
    
    public CloudinaryStorageService(
        @Value("${app.storage.cloudinary.cloud-name:}") String cloudName,
        @Value("${app.storage.cloudinary.api-key:}") String apiKey,
        @Value("${app.storage.cloudinary.api-secret:}") String apiSecret
    ) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret
        ));
    }
    
    @Override
    public String store(String fileName, byte[] data) throws IOException {
        try {
            Map uploadResult = cloudinary.uploader().upload(data, 
                ObjectUtils.asMap("public_id", fileName, "resource_type", "image"));
            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new IOException("Failed to upload to Cloudinary", e);
        }
    }
    
    @Override
    public byte[] retrieve(String fileIdentifier) throws IOException {
        // For Cloudinary, we typically return the URL directly
        // Downloading is handled by the client from the URL
        throw new UnsupportedOperationException("Cloudinary files are accessed via URL");
    }
    
    @Override
    public void delete(String fileIdentifier) throws IOException {
        try {
            // Extract public_id from URL
            String publicId = extractPublicIdFromUrl(fileIdentifier);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new IOException("Failed to delete from Cloudinary", e);
        }
    }
    
    @Override
    public StorageType getStorageType() {
        return StorageType.CLOUDINARY;
    }
    
    private String extractPublicIdFromUrl(String url) {
        // Extract public_id from Cloudinary URL
        // URL format: https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/{public_id}.{format}
        String[] parts = url.split("/");
        String lastPart = parts[parts.length - 1];
        return lastPart.substring(0, lastPart.lastIndexOf('.'));
    }
}
