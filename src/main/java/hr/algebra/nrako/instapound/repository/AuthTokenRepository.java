package hr.algebra.nrako.instapound.repository;

import hr.algebra.nrako.instapound.enums.AuthTokenType;
import hr.algebra.nrako.instapound.model.entity.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
    Optional<AuthToken> findByTokenValue(String tokenValue);
    Optional<AuthToken> findByTokenHash(String tokenHash);
    Optional<AuthToken> findByTokenId(String tokenId);
    List<AuthToken> findAllByUsernameAndRevokedFalse(String username);
    List<AuthToken> findAllByUsernameAndTokenTypeAndRevokedFalse(String username, AuthTokenType tokenType);
}

