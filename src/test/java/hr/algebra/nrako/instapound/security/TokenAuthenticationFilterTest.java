package hr.algebra.nrako.instapound.security;

import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.service.interfaces.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenAuthenticationFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @InjectMocks
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticate_WhenValidBearerTokenProvided() throws ServletException, IOException {
        String validToken = "valid_token";
        String authHeader = "Bearer " + validToken;

        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedpassword")
                .role(hr.algebra.nrako.instapound.enums.UserRole.REGISTERED)
                .build();

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(tokenService.resolveUserByAccessToken(validToken)).thenReturn(java.util.Optional.of(user));

        tokenAuthenticationFilter.doFilter(this.request, this.response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication, "Authentication should be set in SecurityContext");
        assertEquals(authentication.getAuthorities().stream().anyMatch(
                auth -> Objects.equals(auth.getAuthority(), "ROLE_REGISTERED")
                ),
                true, "User should have ROLE_REGISTERED authority");

        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticate_WhenAuthorizationHeaderIsMissing() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        tokenAuthenticationFilter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should not be set in SecurityContext");
        verify(tokenService, never()).resolveUserByAccessToken(anyString());
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticate_WhenHeaderDoesNotStartWithBearer()  throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic invalidToken");

        tokenAuthenticationFilter.doFilter(this.request, this.response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should not be set in SecurityContext");
        verify(tokenService, never()).resolveUserByAccessToken(anyString());
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticate_WhenTokenIsInvalid() throws ServletException, IOException {
        String invalidToken = "invalid_token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(tokenService.resolveUserByAccessToken(invalidToken)).thenReturn(java.util.Optional.empty());

        tokenAuthenticationFilter.doFilter(this.request, this.response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should not be set in SecurityContext");
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldSkipAuthentication_WhenAuthenticationAlreadyExists() throws ServletException, IOException {
        Authentication existingAuthentication = Mockito.mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuthentication);

        tokenAuthenticationFilter.doFilter(this.request, this.response, chain);

        assertEquals(existingAuthentication, SecurityContextHolder.getContext().getAuthentication(), "Existing authentication should remain unchanged");
        verify(request, never()).getHeader(anyString());
        verify(chain).doFilter(request, response);
    }
}