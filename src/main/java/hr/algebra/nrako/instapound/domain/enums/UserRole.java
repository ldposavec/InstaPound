package hr.algebra.nrako.instapound.domain.enums;

/**
 * User roles in the system
 * Represents different authorization levels
 */
public enum UserRole {
    ANONYMOUS,      // Guest users who haven't registered
    REGISTERED,     // Regular authenticated users
    ADMINISTRATOR   // Admin users with elevated privileges
}
