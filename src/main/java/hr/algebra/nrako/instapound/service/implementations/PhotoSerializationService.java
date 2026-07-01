package hr.algebra.nrako.instapound.service.implementations;

import hr.algebra.nrako.instapound.model.serialization.PhotoSnapshot;
import hr.algebra.nrako.instapound.model.serialization.WhitelistedObjectInputStream;
import hr.algebra.nrako.instapound.model.entity.Photo;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@Service
public class PhotoSerializationService {
    private static final byte[] JAVA_SERIALIZATION_MAGIC = new byte[]{(byte) 0xAC, (byte) 0xED, 0x00, 0x05};

    private final Set<String> whitelist = Set.of(
            PhotoSnapshot.class.getName(),
            String.class.getName(),
            Long.class.getName(),
            Number.class.getName(),
            Enum.class.getName(),
            java.util.HashSet.class.getName(),
            java.util.HashMap.class.getName(),
            hr.algebra.nrako.instapound.enums.StorageType.class.getName()
    );

    public Path serializePhotoSnapshot(Photo photo, Path targetFile) throws IOException {
        PhotoSnapshot snapshot = PhotoSnapshot.fromPhoto(photo);
        byte[] bytes = serialize(snapshot);
        Files.createDirectories(targetFile.getParent());
        Files.write(targetFile, bytes);
        return targetFile;
    }

    public PhotoSnapshot deserializePhotoSnapshot(Path file) throws IOException, ClassNotFoundException {
        return deserializePhotoSnapshot(Files.readAllBytes(file));
    }

    public PhotoSnapshot deserializePhotoSnapshot(byte[] bytes) throws IOException, ClassNotFoundException {
        validateMagicBytes(bytes);

        try (ObjectInputStream inputStream = new WhitelistedObjectInputStream(new ByteArrayInputStream(bytes), whitelist)) {
            Object object = inputStream.readObject();
            if (!(object instanceof PhotoSnapshot snapshot)) {
                throw new InvalidClassException("Unexpected deserialized type", object.getClass().getName());
            }
            return snapshot;
        }
    }

    public byte[] serialize(PhotoSnapshot snapshot) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(out)) {
            objectOutputStream.writeObject(snapshot);
            objectOutputStream.flush();
            return out.toByteArray();
        }
    }

    private void validateMagicBytes(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length < JAVA_SERIALIZATION_MAGIC.length) {
            throw new IOException("Invalid serialized file header");
        }
        for (int i = 0; i < JAVA_SERIALIZATION_MAGIC.length; i++) {
            if (bytes[i] != JAVA_SERIALIZATION_MAGIC[i]) {
                throw new IOException("Invalid serialized file header");
            }
        }
    }
}



