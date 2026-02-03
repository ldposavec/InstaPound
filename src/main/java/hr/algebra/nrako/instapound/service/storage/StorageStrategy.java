package hr.algebra.nrako.instapound.service.storage;

import hr.algebra.nrako.instapound.enums.StorageType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface StorageStrategy {
    StorageType getStorageType();
    String store(MultipartFile file, String filename) throws IOException;
    String store(byte[] data, String filename, String contentType) throws IOException;
    InputStream retrieve(String filename) throws IOException;
    void delete(String filename) throws IOException;
    boolean exists(String filename);
    String getUrl(String filename);
}
