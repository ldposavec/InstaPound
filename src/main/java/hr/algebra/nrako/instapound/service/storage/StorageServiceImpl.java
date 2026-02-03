package hr.algebra.nrako.instapound.service.storage;

import hr.algebra.nrako.instapound.config.StorageConfig;
import hr.algebra.nrako.instapound.enums.StorageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StorageServiceImpl {
    private final StorageConfig storageConfig;
    private final Map<StorageType, StorageStrategy> strategies;

    public StorageServiceImpl(StorageConfig storageConfig, List<StorageStrategy> strategyList) {
        this.storageConfig = storageConfig;
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(StorageStrategy::getStorageType, Function.identity()));
        log.info("Storage service initiailized with {} strategies", strategies.size());
    }

    public StorageStrategy getStrategy(StorageType type) {
        StorageStrategy strategy = strategies.get(type);
        if (strategy == null) {
            log.warn("Requested storage type {} not available, falling back to LOCAL", type);
            strategy = strategies.get(StorageType.LOCAL);
        }
        return strategy;
    }

    public String store(MultipartFile file, String filename, StorageType type) throws IOException {
        return getStrategy(type).store(file, filename);
    }

    public String store(byte[] data, String filename, String contentType, StorageType storageType) throws IOException {
        return getStrategy(storageType).store(data, filename, contentType);
    }

    public InputStream retrieve(String filename, StorageType type) throws IOException {
        return getStrategy(type).retrieve(filename);
    }

    public void delete(String filename, StorageType type) throws IOException {
        getStrategy(type).delete(filename);
    }

    public String getUrl(String filename, StorageType type) throws IOException {
        return getStrategy(type).getUrl(filename);
    }
}
