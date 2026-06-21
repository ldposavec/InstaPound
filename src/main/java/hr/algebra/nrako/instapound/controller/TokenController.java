package hr.algebra.nrako.instapound.controller;

import org.springframework.web.bind.annotation.*;
import hr.algebra.nrako.instapound.model.dto.response.TokenPairResponse;
import hr.algebra.nrako.instapound.service.interfaces.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/auth/token")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/refresh")
    public ResponseEntity<Object> refresh(@CookieValue(name = "refresh_token", required = false) String refreshToken,
                                     HttpServletResponse httpResponse) {
        if (refreshToken == null || refreshToken.isBlank())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token is missing");
        try {
            TokenPairResponse tokens = tokenService.rotateRefreshToken(refreshToken);

            Cookie refreshCookie = new Cookie("refresh_token", tokens.getRefreshToken());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(true);
            refreshCookie.setPath("/api/auth/token");
            refreshCookie.setMaxAge(7 * 24 * 60 * 60);
            refreshCookie.setAttribute("SameSite", "Strict");
            httpResponse.addCookie(refreshCookie);

            return ResponseEntity.ok(new AccessTokenResponse(
                    tokens.getAccessToken(),
                    tokens.getTokenType(),
                    tokens.getAccessTokenExpiresInSeconds()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    private record AccessTokenResponse(String accessToken, String tokenType, long accessTokenExpiresInSeconds) {}
}
