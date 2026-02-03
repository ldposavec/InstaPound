package hr.algebra.nrako.instapound.service.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import hr.algebra.nrako.instapound.config.StorageConfig;
import hr.algebra.nrako.instapound.enums.StorageType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "storage.cloudinary.cloud-name")
public class CloudinaryStorageStrategyImpl implements StorageStrategy {
    private final StorageConfig  storageConfig;
    private Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        if (storageConfig.getCloudinary() != null && storageConfig.getCloudinary().getCloudName() != null) {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", storageConfig.getCloudinary().getCloudName(),
                    "api_key", storageConfig.getCloudinary().getApiKey(),
                    "api_secret", storageConfig.getCloudinary().getApiSecret(),
                    "secure", true
            ));
            log.info("Cloudinary storage initialized with cloud name: {}", storageConfig.getCloudinary().getCloudName());
        }
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.CLOUDINARY;
    }

    @Override
    public String store(MultipartFile file, String filename) throws IOException {
        return store(file.getBytes(), filename, file.getContentType());
    }

    @Override
    public String store(byte[] data, String filename, String contentType) throws IOException {
        if (cloudinary == null) throw new IOException("Cloudinary is not configured");
        try {
            String publicId = getPublicIdFromFilename(filename);
            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", "image",
                    "folder", storageConfig.getCloudinary().getFolder()
            );

            Map<String, Object> result = cloudinary.uploader().upload(data, uploadOptions);
            String secureUrl = (String) result.get("secure_url");
            log.info("Stored file to Cloudinary: {} -> {}", filename, secureUrl);
            return filename;
        } catch (Exception e) {
            log.error("Error uploading to Cloudinary: {}", e.getMessage());
            throw new IOException("Failed to uploading to Cloudinary: " + e.getMessage(), e);
        }
    }

    private String getPublicIdFromFilename(String filename) {
        if (filename.contains(".")) return filename.substring(0, filename.indexOf("."));
        return filename;
    }

    @Override
    public InputStream retrieve(String filename) throws IOException {
        if (cloudinary == null) throw new IOException("Cloudinary is not configured");
        try {
            String url = getUrl(filename);
//            return URI.create(url).toURL().openStream();
            URI uri = URI.create(url);
            String host = uri.getHost();
            if (host == null || !host.endsWith("cloudinary.com")) throw new IOException("Invalid cloudinary URL: " + url);
            return uri.toURL().openStream();
        } catch (Exception e) {
            log.error("Error retrieving file from Cloudinary: {}", e.getMessage());
            throw new IOException("Failed to retrieve file from Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String filename) throws IOException {
        if (cloudinary == null) throw new IOException("Cloudinary is not configured");
        try {
            String publicId = getFullPublicId(filename);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted file from Cloudinary: {}", filename);
        } catch (Exception e) {
            log.error("Error deleting file from Cloudinary: {}", e.getMessage());
            throw new IOException("Failed to delete file from Cloudinary: " + e.getMessage(), e);
        }
    }

    private String getFullPublicId(String filename) {
        String publicId = getPublicIdFromFilename(filename);
        String folder = storageConfig.getCloudinary().getFolder();
        if (folder != null && !folder.isEmpty()) return folder + "/" + publicId;
        return publicId;
    }

    @Override
    public boolean exists(String filename) {
        if (cloudinary == null) return false;
        try {
            String publicId = getFullPublicId(filename);
            Map<String, Object> result = cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
            return result != null && result.containsKey("public_id");
        } catch (Exception e) {
            log.warn("Error checking if file exists in Cloudinary: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getUrl(String filename) {
        if (cloudinary == null) return null;

        String publicId = getFullPublicId(filename);
        return cloudinary.url().secure(true).resourceType("image").generate(publicId);
    }
}
