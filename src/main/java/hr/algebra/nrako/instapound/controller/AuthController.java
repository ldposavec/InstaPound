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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActionLogService actionLogService;
    private final UserMapper userMapper;

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
