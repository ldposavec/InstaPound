package hr.algebra.nrako.instapound.model.serialization;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.Set;

public class WhitelistedObjectInputStream extends ObjectInputStream {
    private final Set<String> allowedClassNames;

    public WhitelistedObjectInputStream(InputStream in, Set<String> allowedClassNames) throws IOException {
        super(in);
        this.allowedClassNames = allowedClassNames;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        String name = desc.getName();
        if (!allowedClassNames.contains(name) && !name.startsWith("[")) {
            throw new InvalidClassException("Unauthorized deserialization attempt", name);
        }
        return super.resolveClass(desc);
    }
}

