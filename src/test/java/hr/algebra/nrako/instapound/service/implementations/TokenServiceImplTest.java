package hr.algebra.nrako.instapound.service.implementations;

import hr.algebra.nrako.instapound.enums.AuthTokenType;
import hr.algebra.nrako.instapound.enums.UserRole;
import hr.algebra.nrako.instapound.model.dto.response.TokenPairResponse;
import hr.algebra.nrako.instapound.model.entity.AuthToken;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.repository.AuthTokenRepository;
import hr.algebra.nrako.instapound.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TokenServiceImplTest {

    @Mock
    private AuthTokenRepository authTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TokenServiceImpl tokenService;

    private User user;
    private final AtomicReference<AuthToken> refreshTokenRef = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("alice")
                .role(UserRole.REGISTERED)
                .password("encoded")
                .build();

        when(userRepository.findByUsername("alice")).thenReturn(user);
        when(authTokenRepository.save(ArgumentMatchers.any(AuthToken.class))).thenAnswer(invocation -> {
            AuthToken token = invocation.getArgument(0);
            if (token.getTokenType() == AuthTokenType.REFRESH) {
                refreshTokenRef.set(token);
            }
            return token;
        });
        when(authTokenRepository.findAllByUsernameAndRevokedFalse("alice")).thenReturn(List.of());
    }

    @Test
    void issueTokenPair_shouldCreateTwoDistinctTokens() {
        TokenPairResponse response = tokenService.issueTokenPair(user);

        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotEquals(response.getAccessToken(), response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertTrue(response.getAccessTokenExpiresInSeconds() > 0);
        assertTrue(response.getRefreshTokenExpiresInSeconds() > response.getAccessTokenExpiresInSeconds());
        verify(authTokenRepository, times(2)).save(any(AuthToken.class));
    }

    @Test
    void rotateRefreshToken_shouldRevokeOldTokenAndIssuesNewPair() {
        TokenPairResponse initial = tokenService.issueTokenPair(user);
        AuthToken storedRefresh = refreshTokenRef.get();
        when(authTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedRefresh));

        TokenPairResponse rotated = tokenService.rotateRefreshToken(initial.getRefreshToken());

        assertNotNull(rotated.getAccessToken());
        assertNotNull(rotated.getRefreshToken());
        assertTrue(storedRefresh.isRevoked());
        assertTrue(storedRefresh.isExpired());
        verify(authTokenRepository, atLeast(3)).save(any(AuthToken.class));
    }

    @Test
    void reuseOfRefreshToken_shouldRevokeAllUserTokens() {
        TokenPairResponse initial = tokenService.issueTokenPair(user);
        AuthToken storedRefresh = refreshTokenRef.get();
        storedRefresh.setRevoked(true);
        storedRefresh.setExpired(true);
        when(authTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedRefresh));
        when(authTokenRepository.findAllByUsernameAndRevokedFalse("alice")).thenReturn(List.of(storedRefresh));

        String refreshToken = initial.getRefreshToken();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> tokenService.rotateRefreshToken(refreshToken));

        assertTrue(ex.getMessage().contains("Refresh token reuse detected"));
        verify(authTokenRepository).saveAll(anyList());
    }

    @Test
    void resolveUserByAccessToken_shouldReturnEmptyForNullToken() {
        Optional<User> result = tokenService.resolveUserByAccessToken(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void resoleUesrByAccessToken_shouldReturnEmptyForBlankToken() {
        Optional<User> result = tokenService.resolveUserByAccessToken("");
        assertTrue(result.isEmpty());
    }

    @Test
    void resolveUserByRefreshToken_shouldReturnUserForValidToken() {
        AuthToken validToken = AuthToken.builder()
                .tokenId("id").tokenValue("val").tokenHash("hash")
                .tokenType(AuthTokenType.ACCESS)
                .revoked(false).expired(false)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .username("alice")
                .build();

        when(authTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(validToken));
        when(userRepository.findByUsername("alice")).thenReturn(user);

        Optional<User> result = tokenService.resolveUserByAccessToken("valid-access-token");

        assertTrue(result.isPresent());
        assertEquals("alice", result.get().getUsername());
    }
}


