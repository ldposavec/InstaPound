package hr.algebra.nrako.instapound.exception;

/**
 * Exception thrown when package limits are exceeded
 */
public class PackageLimitExceededException extends RuntimeException {
    
    public PackageLimitExceededException(String message) {
        super(message);
    }
}
