package hr.algebra.nrako.instapound.domain.enums;

/**
 * Authentication provider types
 * Users can authenticate using different methods
 */
public enum AuthProvider {
    LOCAL,   // Local account created in the application
    GOOGLE,  // Google OAuth2 authentication
    GITHUB   // GitHub OAuth2 authentication
}
