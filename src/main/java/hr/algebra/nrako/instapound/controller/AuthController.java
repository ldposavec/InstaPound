package hr.algebra.nrako.instapound.controller;

import hr.algebra.nrako.instapound.enums.ActionType;
import hr.algebra.nrako.instapound.enums.UserRole;
import hr.algebra.nrako.instapound.model.dto.request.UserRegistrationRequest;
import hr.algebra.nrako.instapound.model.dto.response.UserResponse;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.mappers.UserMapper;
import hr.algebra.nrako.instapound.repository.UserRepository;
import hr.algebra.nrako.instapound.service.implementations.UserServiceImpl;
import hr.algebra.nrako.instapound.service.interfaces.ActionLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActionLogService actionLogService;
    private final UserMapper userMapper;

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

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
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        actionLogService.logAction(user, ActionType.USER_REGISTER,
                "User registered with package: " + request.getPackageType(), getCientIp(httpRequest));
        log.info("New user registered: {}",  user.getUsername());
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

    /**
     * Returns information about OAuth2 login options and how to use them.
     * 
     * OAuth2 Login Flow (Okta):
     * 1. Navigate to GET /oauth2/authorization/okta in a browser
     * 2. User is redirected to Okta login page
     * 3. After successful login, user is redirected back to /login/oauth2/code/okta
     * 4. Application exchanges the code for tokens and creates/updates user
     * 5. User is redirected to /api/user/profile
     * 
     * Note: OAuth2 login requires a browser - it cannot be done via pure API calls
     * because it involves redirects to the OAuth2 provider's login page.
     */
    @GetMapping("/oauth2/info")
    public ResponseEntity<Map<String, Object>> getOAuth2Info(HttpServletRequest request) {
        Map<String, Object> info = new HashMap<>();
        String baseUrl = getBaseUrl(request);
        
        info.put("oauth2Enabled", clientRegistrationRepository != null);
        
        if (clientRegistrationRepository != null) {
            List<Map<String, String>> providers = new ArrayList<>();
            
            // Try to get Okta registration
            try {
                ClientRegistration okta = clientRegistrationRepository.findByRegistrationId("okta");
                if (okta != null) {
                    Map<String, String> oktaInfo = new HashMap<>();
                    oktaInfo.put("provider", "okta");
                    oktaInfo.put("loginUrl", baseUrl + "/oauth2/authorization/okta");
                    oktaInfo.put("description", "Click or navigate to loginUrl in a browser to start Okta OAuth2 login");
                    providers.add(oktaInfo);
                }
            } catch (Exception e) {
                log.debug("Okta registration not found: {}", e.getMessage());
            }
            
            info.put("providers", providers);
            info.put("instructions", List.of(
                "1. OAuth2 login requires a web browser - it cannot be done via pure API/curl calls",
                "2. Navigate to the 'loginUrl' in a web browser",
                "3. You will be redirected to the OAuth2 provider (Okta) login page",
                "4. Enter your credentials on the provider's page",
                "5. After successful login, you'll be redirected back to the application",
                "6. The application will create/update your user and establish a session",
                "7. You can then access protected endpoints using the session cookie"
            ));
            info.put("testingWithCurl", Map.of(
                "note", "For API testing, use local account login instead",
                "registerEndpoint", "POST " + baseUrl + "/api/auth/register",
                "loginEndpoint", "POST " + baseUrl + "/api/auth/login (form data: username, password)",
                "example", "curl -X POST " + baseUrl + "/api/auth/login -d 'username=testuser&password=user123' -c cookies.txt"
            ));
        } else {
            info.put("message", "OAuth2 is not configured. To enable Okta OAuth2, configure the following properties in application.properties:");
            info.put("requiredProperties", List.of(
                "spring.security.oauth2.client.registration.okta.client-id",
                "spring.security.oauth2.client.registration.okta.client-secret",
                "spring.security.oauth2.client.registration.okta.scope=openid,profile,email",
                "spring.security.oauth2.client.provider.okta.issuer-uri"
            ));
        }
        
        return ResponseEntity.ok(info);
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        
        if ((scheme.equals("http") && serverPort != 80) || 
            (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort);
        }
        
        return url.toString();
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

    private String getCientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) return xForwardedFor.split(",")[0].trim();
        return request.getRemoteAddr();
    }

    private record UsernameCheckResponse(boolean available) {}
    private record EmailCheckResponse(boolean available) {}
}
