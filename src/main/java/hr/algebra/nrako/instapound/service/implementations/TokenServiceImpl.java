package hr.algebra.nrako.instapound.service.implementations;

import hr.algebra.nrako.instapound.enums.AuthTokenType;
import hr.algebra.nrako.instapound.model.dto.response.TokenPairResponse;
import hr.algebra.nrako.instapound.model.entity.AuthToken;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.repository.AuthTokenRepository;
import hr.algebra.nrako.instapound.repository.UserRepository;
import hr.algebra.nrako.instapound.service.interfaces.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private static final long ACCESS_TOKEN_TTL_SECONDS = 15L * 60;
    private static final long REFRESH_TOKEN_TTL_SECONDS = 7L * 24 * 60 * 60;

    private final AuthTokenRepository authTokenRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public TokenPairResponse issueTokenPair(User user) {
        AuthToken accessToken = createAndPersistToken(user, AuthTokenType.ACCESS, ACCESS_TOKEN_TTL_SECONDS);
        AuthToken refreshToken = createAndPersistToken(user, AuthTokenType.REFRESH, REFRESH_TOKEN_TTL_SECONDS);
        return toResponse(accessToken, refreshToken);
    }

    @Override
    public TokenPairResponse rotateRefreshToken(String refreshToken) {
        AuthToken storedToken = authTokenRepository.findByTokenHash(hashToken(refreshToken))
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        if (storedToken.isRevoked() || storedToken.isExpired() || storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            revokeAllTokensForUser(storedToken.getUsername());
            throw new IllegalStateException("Refresh token reuse detected. All tokens for the user were revoked.");
        }

        storedToken.setRevoked(true);
        storedToken.setExpired(true);
        authTokenRepository.save(storedToken);

        User user = userRepository.findByUsername(storedToken.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("User for refresh token not found");
        }

        return issueTokenPair(user);
    }

    @Override
    public void revokeAllTokensForUser(String username) {
        List<AuthToken> activeTokens = authTokenRepository.findAllByUsernameAndRevokedFalse(username);
        for (AuthToken token : activeTokens) {
            token.setRevoked(true);
            token.setExpired(true);
        }
        authTokenRepository.saveAll(activeTokens);
    }

    @Override
    public Optional<User> resolveUserByAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return Optional.empty();
        }

        return authTokenRepository.findByTokenHash(hashToken(accessToken))
                .filter(token -> token.getTokenType() == AuthTokenType.ACCESS)
                .filter(token -> !token.isRevoked())
                .filter(token -> !token.isExpired())
                .filter(token -> token.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(token -> userRepository.findByUsername(token.getUsername()));
    }

    private AuthToken createAndPersistToken(User user, AuthTokenType tokenType, long ttlSeconds) {
        String rawToken = generateTokenValue();
        LocalDateTime issuedAt = LocalDateTime.now();
        AuthToken token = AuthToken.builder()
                .tokenId(UUID.randomUUID().toString())
                .tokenValue(rawToken)
                .tokenHash(hashToken(rawToken))
                .tokenType(tokenType)
                .revoked(false)
                .expired(false)
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(ttlSeconds))
                .username(user.getUsername())
                .build();
        return authTokenRepository.save(token);
    }

    private TokenPairResponse toResponse(AuthToken accessToken, AuthToken refreshToken) {
        return TokenPairResponse.builder()
                .accessToken(accessToken.getTokenValue())
                .refreshToken(refreshToken.getTokenValue())
                .tokenType("Bearer")
                .accessTokenExpiresInSeconds(ACCESS_TOKEN_TTL_SECONDS)
                .refreshTokenExpiresInSeconds(REFRESH_TOKEN_TTL_SECONDS)
                .build();
    }

    private String generateTokenValue() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes) + "." + UUID.randomUUID();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}

