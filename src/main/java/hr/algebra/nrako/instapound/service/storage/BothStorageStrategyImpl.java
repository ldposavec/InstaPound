package hr.algebra.nrako.instapound.service.storage;

import hr.algebra.nrako.instapound.enums.StorageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@ConditionalOnBean(CloudinaryStorageStrategyImpl.class)
@Slf4j
public class BothStorageStrategyImpl implements StorageStrategy {
    private final LocalStorageStrategyImpl local;
    private final CloudinaryStorageStrategyImpl cloudinary;

    @Autowired
    public BothStorageStrategyImpl(LocalStorageStrategyImpl local, CloudinaryStorageStrategyImpl cloudinary) {
        this.local = local;
        this.cloudinary = cloudinary;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.BOTH;
    }

    @Override
    public String store(MultipartFile file, String filename) throws IOException {
        local.store(file, filename);
        return cloudinary.store(file, filename);
    }

    @Override
    public String store(byte[] data, String filename, String contentType) throws IOException {
        local.store(data, filename, contentType);
        return cloudinary.store(data, filename, contentType);
    }

    @Override
    public InputStream retrieve(String filename) throws IOException {
        if (local.exists(filename)) {
            return local.retrieve(filename);
        }
        return cloudinary.retrieve(filename);
    }

    @Override
    public void delete(String filename) throws IOException {
        local.delete(filename);
        try {
            cloudinary.delete(filename);
        } catch (IOException _) {
            log.warn("Could not delete file from Cloudinary: {}", filename);
        }
    }

    @Override
    public boolean exists(String filename) {
        return local.exists(filename) || cloudinary.exists(filename);
    }

    @Override
    public String getUrl(String filename) {
        return cloudinary.getUrl(filename);
    }
}
