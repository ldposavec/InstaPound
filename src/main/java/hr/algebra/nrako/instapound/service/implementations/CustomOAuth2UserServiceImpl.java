package hr.algebra.nrako.instapound.service.implementations;

import hr.algebra.nrako.instapound.enums.AuthProvider;
import hr.algebra.nrako.instapound.enums.PackageType;
import hr.algebra.nrako.instapound.enums.UserRole;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.valueobject.PackageUsage;
import hr.algebra.nrako.instapound.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.security.oauth2.client.registration.okta.client-id")
public class CustomOAuth2UserServiceImpl extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        OAuth2User oAuth2User = super.loadUser(userRequest);
//
//        String registrationId = userRequest.getClientRegistration().getRegistrationId();
//        AuthProvider authProvider = AuthProvider.valueOf(registrationId.toLowerCase());
//
//        String email = oAuth2User.getAttribute("email");
//        String name  = oAuth2User.getAttribute("name");
//        String providerId = oAuth2User.getAttribute("sub");
//        if (providerId == null) providerId = String.valueOf(oAuth2User.getAttribute("id"));
//
//        User user = userRepository.findByEmail(email);
//        if (user == null) {
//            user = User.builder()
//                    .username(generateUsername(name, email))
//                    .email(email)
//                    .authProvider(authProvider)
//                    .providerId(providerId)
//                    .role(UserRole.REGISTERED)
//                    .createdAt(LocalDateTime.now())
//                    .lastLoginAt(LocalDateTime.now())
//                    .build();
//            userRepository.save(user);
//            log.info("Created new user via OAuth2: {}", email);
//        } else {
//            user.setLastLoginAt(LocalDateTime.now());
//            userRepository.save(user);
//            log.info("OAuth2 login for existing user: {}", email);
//        }
//
//        return oAuth2User;
//    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider authProvider = getAuthProvider(registrationId);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = extractEmail(attributes, registrationId);
        String name = extractName(attributes, registrationId);
        String providerId = extractProviderId(attributes, registrationId);

        if (email == null || email.isBlank()) {
            log.error("Email not found in OAuth2 user attributes for provider: {}", registrationId);
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = User.builder()
                    .username(generateUsername(name, email))
                    .email(email)
                    .authProvider(authProvider)
                    .providerId(providerId)
                    .packageType(PackageType.FREE)
                    .packageUsage(new PackageUsage())
                    .role(UserRole.REGISTERED)
                    .createdAt(LocalDateTime.now())
                    .lastLoginAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);
            log.info("Created new user via OAuth2 ({}): {}", registrationId, email);
        } else {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("OAuth2 login ({}) for existing user: {}", registrationId, email);
        }

        return oAuth2User;
    }

    private String extractProviderId(Map<String, Object> attributes, String registrationId) {
        if (attributes.containsKey("sub")) return (String) attributes.get("sub");
        if (attributes.containsKey("id")) {
            Object id = attributes.get("id");
            return id != null ? String.valueOf(id) : null;
        }
        return null;
    }

    private String extractName(Map<String, Object> attributes, String registrationId) {
        if (attributes.containsKey("name")) return (String) attributes.get("name");
        if (attributes.containsKey("given_name")) {
            String givenName = (String) attributes.get("given_name");
            String familyName = (String) attributes.get("family_name");
            if (familyName != null) return givenName + " " + familyName;
            return givenName;
        }
        if (attributes.containsKey("preferred_username")) return (String) attributes.get("preferred_username");
        return null;
    }

    private String extractEmail(Map<String, Object> attributes, String registrationId) {
        if (attributes.containsKey("email")) return (String) attributes.get("email");
        if (attributes.containsKey("preferred_username")) {
            String preferredUsername = (String) attributes.get("preferred_username");
            if (preferredUsername != null && preferredUsername.contains("@")) return preferredUsername;
        }
        return null;
    }

    private AuthProvider getAuthProvider(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "okta" -> AuthProvider.OKTA;
            case "github" -> AuthProvider.GITHUB;
            case "google" -> AuthProvider.GOOGLE;
            default -> AuthProvider.LOCAL;
        };
    }

    private String generateUsername(String name, String email) {
        if (name != null && !name.isBlank()) return name.replaceAll("\\s+", "_");
        if (email != null && email.contains("@")) return email.split("@")[0];
        return "user_" + System.currentTimeMillis();
    }
}
