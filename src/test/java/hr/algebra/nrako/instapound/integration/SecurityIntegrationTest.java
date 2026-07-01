package hr.algebra.nrako.instapound.integration;

import hr.algebra.nrako.instapound.config.TestSecurityConfig;
import hr.algebra.nrako.instapound.enums.AuthTokenType;
import hr.algebra.nrako.instapound.enums.PackageType;
import hr.algebra.nrako.instapound.enums.UserRole;
import hr.algebra.nrako.instapound.model.dto.response.TokenPairResponse;
import hr.algebra.nrako.instapound.model.entity.AuthToken;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.valueobject.PackageUsage;
import hr.algebra.nrako.instapound.repository.AuthTokenRepository;
import hr.algebra.nrako.instapound.repository.UserRepository;
import hr.algebra.nrako.instapound.service.interfaces.TokenService;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @Autowired
    private TokenService tokenService;

    private User registeredUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        registeredUser = userRepository.save(User.builder()
                .username("registeredUser")
                .email("registered@test.com")
                .password("password123")
                .role(UserRole.REGISTERED)
                .packageType(PackageType.FREE)
                .packageUsage(new PackageUsage())
                .createdAt(java.time.LocalDateTime.now())
                .build());
        adminUser = userRepository.save(User.builder()
                .username("adminUser")
                .email("admin@test.com")
                .password("admin123")
                .role(UserRole.ADMIN)
                .packageType(PackageType.GOLD)
                .packageUsage(new PackageUsage())
                .createdAt(java.time.LocalDateTime.now())
                .build());
    }

    @Test
    void testUnauthenticatedAccessRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void testUserCannotAccessAdminEndpoint() throws Exception {
//        String rawToken = "user-access-token-" + UUID.randomUUID().toString();
//        createAndPersistAccessToken(rawToken, registeredUser.getUsername(), LocalDateTime.now().plusMinutes(15));
//
//        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + rawToken))
//                .andExpect(status().isForbidden());

        TokenPairResponse tokens = tokenService.issueTokenPair(registeredUser);
        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminCanAccessAdminEndpoint() throws Exception {
        TokenPairResponse tokens = tokenService.issueTokenPair(adminUser);

        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isOk());
    }

    @Test
    void testExpiredTokenRedirectsToLogin() throws Exception {
        TokenPairResponse tokens = tokenService.issueTokenPair(registeredUser);

        List<AuthToken> userTokens = authTokenRepository.findAllByUsernameAndRevokedFalse(registeredUser.getUsername());
        for (AuthToken token : userTokens) {
            if (token.getTokenValue().equals(tokens.getAccessToken())) {
                token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
                token.setExpired(true);
                authTokenRepository.save(token);
                break;
            }
        }

        mockMvc.perform(get("/api/user/profile").header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
