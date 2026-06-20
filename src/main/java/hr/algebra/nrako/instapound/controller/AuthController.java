package hr.algebra.nrako.instapound.controller;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import hr.algebra.nrako.instapound.enums.ActionType;
import hr.algebra.nrako.instapound.enums.AuthProvider;
import hr.algebra.nrako.instapound.enums.UserRole;
import hr.algebra.nrako.instapound.model.dto.request.UserRegistrationRequest;
import hr.algebra.nrako.instapound.model.dto.request.LoginRequest;
import hr.algebra.nrako.instapound.model.dto.response.TokenPairResponse;
import hr.algebra.nrako.instapound.model.mappers.UserMapper;
import hr.algebra.nrako.instapound.repository.UserRepository;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.service.interfaces.ActionLogService;
import hr.algebra.nrako.instapound.service.interfaces.TokenService;
import hr.algebra.nrako.instapound.utils.IpUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActionLogService actionLogService;
    private final TokenService tokenService;
    private final UserMapper userMapper;
    private final IpUtils ipUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse httpResponse) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = userRepository.findByUsername(request.getUsername());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }

            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            actionLogService.logAction(user, ActionType.USER_LOGIN,
                    "User logged in", ipUtils.getClientIp(httpRequest));
            TokenPairResponse tokens = tokenService.issueTokenPair(user);

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
        } catch (Exception e) {
            log.warn("Login failed for username {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationRequest request,
                                      HttpServletRequest httpRequest) {
        if (userRepository.existsByUsername(request.getUsername())) return ResponseEntity.badRequest().body("Username" +
                " is already taken");
        if (userRepository.existsByEmail(request.getEmail())) return ResponseEntity.badRequest().body("Email is already in use");

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.REGISTERED)
                .packageType(request.getPackageType())
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        actionLogService.logAction(user, ActionType.USER_REGISTER,
                "User registered with package: " + request.getPackageType(), ipUtils.getClientIp(httpRequest));
        log.info("New user registered: {}", user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDto(user));
    }

    @GetMapping("/check-username/{username}")
    public ResponseEntity<?> checkUsername(@PathVariable String username) {
        boolean available = !userRepository.existsByUsername(username);
        return ResponseEntity.ok().body(new UsernameCheckResponse(available));
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<?> checkEmail(@PathVariable String email) {
        boolean available = !userRepository.existsByEmail(email);
        return ResponseEntity.ok().body(new EmailCheckResponse(available));
    }

//    private UserResponse toDto(User user) {
//        return UserResponse.builder()
//                .id(user.getId())
//                .username(user.getUsername())
//                .email(user.getEmail())
//                .role(user.getRole())
//                .packageType(user.getPackageType())
//                .packageUsage(user.getPackageUsage())
//                .pendingPackageType(user.getPendingPackageType())
//                .createdAt(user.getCreatedAt())
//                .lastLoginAt(user.getLastLoginAt())
//                .build();
//    }

//    private String getCientIp(HttpServletRequest request) {
//        String xForwardedFor = request.getHeader("X-Forwarded-For");
//        if (xForwardedFor != null && !xForwardedFor.isEmpty()) return xForwardedFor.split(",")[0].trim();
//        return request.getRemoteAddr();
//    }

    private record UsernameCheckResponse(boolean available) {}
    private record EmailCheckResponse(boolean available) {}
    private record AccessTokenResponse(String accessToken, String tokenType, long accessTokenExpiresInSeconds) {}
}
