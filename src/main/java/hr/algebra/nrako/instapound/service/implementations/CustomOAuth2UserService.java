package hr.algebra.nrako.instapound.service.implementations;

import hr.algebra.nrako.instapound.enums.AuthProvider;
import hr.algebra.nrako.instapound.enums.UserRole;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Custom OAuth2UserService for Google and GitHub authentication
 * Part of the Strategy pattern for different authentication providers
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub"); // Google
        if (providerId == null) {
            providerId = String.valueOf(oAuth2User.getAttribute("id")); // GitHub
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = User.builder()
                    .username(generateUsername(name, email))
                    .email(email)
                    .authProvider(provider)
                    .providerId(providerId)
                    .role(UserRole.REGISTERED)
                    .createdAt(LocalDateTime.now())
                    .lastLoginAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);
            log.info("Created new user via OAuth2: {}", email);
        } else {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("OAuth2 login for existing user: {}", email);
        }

        return oAuth2User;
    }

    private String generateUsername(String name, String email) {
        if (name != null && !name.isBlank()) {
            return name.replaceAll("\\s+", "_").toLowerCase();
        }
        return email.split("@")[0];
    }
}
