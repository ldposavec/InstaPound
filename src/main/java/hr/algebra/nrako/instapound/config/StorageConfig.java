package hr.algebra.nrako.instapound.config;

import hr.algebra.nrako.instapound.enums.StorageType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "storage")
@Data
public class StorageConfig {
    private StorageType type = StorageType.LOCAL;
    private String localPath = "./uploads";
    private String thumbnailPath = "./uploads/thumbnails";
    private CloudinaryConfig cloudinary;

    @Data
    public static class CloudinaryConfig {
        private String cloudName;
        private String apiKey;
        private String apiSecret;
        private String folder = "instapound";
    }
}
