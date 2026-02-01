package hr.algebra.nrako.instapound.config;

import hr.algebra.nrako.instapound.enums.StorageType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Storage configuration properties - supports local and cloud storage
 */
@Configuration
@ConfigurationProperties(prefix = "storage")
@Getter
@Setter
public class StorageConfig {
    private StorageType type = StorageType.LOCAL;
    private String localPath = "./uploads";
    private String thumbnailPath = "./uploads/thumbnails";
    
    // AWS S3 Configuration
    private String s3BucketName;
    private String s3Region;
    private String s3AccessKey;
    private String s3SecretKey;
}
