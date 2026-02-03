package hr.algebra.nrako.instapound.utils;

import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

/**
 * Utility class for extracting user information from different authentication types.
 * Handles both form-based login (UserDetails) and OAuth2 login (OAuth2User).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthUtils {
    private final UserRepository userRepository;

    /**
     * Extracts the User entity from an Authentication object.
     * Works with both UserDetails (form login) and OAuth2User (OAuth2 login).
     *
     * @param authentication The Spring Security Authentication object
     * @return The User entity, or null if not found
     */
    public User getUserFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetails userDetails) {
            // Form-based login - find by username
            return userRepository.findByUsername(userDetails.getUsername());
        } else if (principal instanceof OAuth2User oAuth2User) {
            // OAuth2 login - find by email
            String email = oAuth2User.getAttribute("email");
            if (email == null) {
                // Try preferred_username for some OAuth providers
                String preferredUsername = oAuth2User.getAttribute("preferred_username");
                if (preferredUsername != null && preferredUsername.contains("@")) {
                    email = preferredUsername;
                }
            }
            if (email != null) {
                return userRepository.findByEmail(email);
            }
            log.warn("Could not extract email from OAuth2User: {}", oAuth2User.getAttributes());
        }
        
        return null;
    }
}
