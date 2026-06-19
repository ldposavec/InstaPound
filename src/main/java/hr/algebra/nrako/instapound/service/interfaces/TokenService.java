package hr.algebra.nrako.instapound.service.interfaces;

import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.dto.response.TokenPairResponse;

import java.util.Optional;

public interface TokenService {
    TokenPairResponse issueTokenPair(User user);
    TokenPairResponse rotateRefreshToken(String refreshToken);
    void revokeAllTokensForUser(String username);
    Optional<User> resolveUserByAccessToken(String accessToken);
}


