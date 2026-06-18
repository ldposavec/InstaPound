package hr.algebra.nrako.instapound.model.entity;

import hr.algebra.nrako.instapound.enums.AuthTokenType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "AUTH_TOKENS")
public class AuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
    private String tokenId;

    @Column(nullable = false, unique = true, length = 256)
    private String tokenValue;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthTokenType tokenType;

    @Column(nullable = false)
    private boolean revoked;

    @Column(nullable = false)
    private boolean expired;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String tokenHash;
}
