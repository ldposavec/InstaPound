package hr.algebra.nrako.instapound.service.storage;

import hr.algebra.nrako.instapound.config.StorageConfig;
import hr.algebra.nrako.instapound.enums.StorageType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;

/**
 * S3 Cloud Storage Implementation - Concrete Strategy for Strategy Pattern
 * Stores files on Amazon S3
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageStrategy implements StorageStrategy {

    private final StorageConfig storageConfig;
    private S3Client s3Client;

    @PostConstruct
    public void init() {
        if (storageConfig.getS3AccessKey() != null && !storageConfig.getS3AccessKey().isBlank()) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(
                    storageConfig.getS3AccessKey(),
                    storageConfig.getS3SecretKey()
            );
            
            s3Client = S3Client.builder()
                    .region(Region.of(storageConfig.getS3Region() != null ? storageConfig.getS3Region() : "us-east-1"))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();
            
            log.info("S3 storage initialized for bucket: {}", storageConfig.getS3BucketName());
        } else {
            log.info("S3 storage not configured - will use local storage");
        }
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.S3;
    }

    @Override
    public String store(MultipartFile file, String filename) throws IOException {
        if (s3Client == null) {
            throw new IOException("S3 client not configured");
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(storageConfig.getS3BucketName())
                .key(filename)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        
        log.info("Stored file to S3: {}", filename);
        return filename;
    }

    @Override
    public String store(byte[] data, String filename, String contentType) throws IOException {
        if (s3Client == null) {
            throw new IOException("S3 client not configured");
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(storageConfig.getS3BucketName())
                .key(filename)
                .contentType(contentType)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(data));
        
        log.info("Stored processed file to S3: {}", filename);
        return filename;
    }

    @Override
    public InputStream retrieve(String filename) throws IOException {
        if (s3Client == null) {
            throw new IOException("S3 client not configured");
        }

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(storageConfig.getS3BucketName())
                .key(filename)
                .build();

        return s3Client.getObject(request);
    }

    @Override
    public void delete(String filename) throws IOException {
        if (s3Client == null) {
            throw new IOException("S3 client not configured");
        }

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(storageConfig.getS3BucketName())
                .key(filename)
                .build();

        s3Client.deleteObject(request);
        log.info("Deleted file from S3: {}", filename);
    }

    @Override
    public boolean exists(String filename) {
        if (s3Client == null) {
            return false;
        }

        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(storageConfig.getS3BucketName())
                    .key(filename)
                    .build();
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    @Override
    public String getUrl(String filename) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                storageConfig.getS3BucketName(),
                storageConfig.getS3Region(),
                filename);
    }
}
