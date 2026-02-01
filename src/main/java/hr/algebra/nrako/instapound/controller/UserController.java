package hr.algebra.nrako.instapound.controller;

import hr.algebra.nrako.instapound.enums.ActionType;
import hr.algebra.nrako.instapound.model.dto.PackageChangeRequest;
import hr.algebra.nrako.instapound.model.dto.PackageInfoResponse;
import hr.algebra.nrako.instapound.model.dto.UserResponse;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.repository.UserRepository;
import hr.algebra.nrako.instapound.service.interfaces.ActionLogService;
import hr.algebra.nrako.instapound.service.interfaces.UserPackageService;
import hr.algebra.nrako.instapound.service.interfaces.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * User Controller - handles user profile and package management
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserPackageService userPackageService;
    private final ActionLogService actionLogService;

    /**
     * Get current user's profile
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUserProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapToResponse(user));
    }

    /**
     * Get user profile by username
     */
    @GetMapping("/profile/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        Optional<User> userOpt = userService.getByUsername(username);
        return userOpt.map(user -> ResponseEntity.ok(mapToResponse(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get current user's package info with usage tracking
     */
    @GetMapping("/package")
    @PreAuthorize("hasAnyRole('REGISTERED', 'ADMIN')")
    public ResponseEntity<PackageInfoResponse> getCurrentPackage(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userPackageService.getPackageInfoForUser(user));
    }

    /**
     * Get all available packages
     */
    @GetMapping("/packages")
    public ResponseEntity<List<PackageInfoResponse>> getAllPackages() {
        return ResponseEntity.ok(userPackageService.getAllPackages());
    }

    /**
     * Request a package change (effective next day)
     */
    @PostMapping("/package/change")
    @PreAuthorize("hasAnyRole('REGISTERED', 'ADMIN')")
    public ResponseEntity<?> requestPackageChange(
            @RequestBody PackageChangeRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {
        
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        if (!userPackageService.canChangePackage(user)) {
            return ResponseEntity.badRequest()
                    .body("You can only change your package once per day. Please try again tomorrow.");
        }

        boolean success = userPackageService.requestPackageChange(user, request.getNewPackage());
        if (success) {
            log.info("User {} requested package change to {}", user.getUsername(), request.getNewPackage());
            return ResponseEntity.ok().body("Package change requested. New package will be effective tomorrow.");
        } else {
            return ResponseEntity.badRequest().body("Unable to change package. You may already have this package.");
        }
    }

    /**
     * Cancel pending package change
     */
    @DeleteMapping("/package/change")
    @PreAuthorize("hasAnyRole('REGISTERED', 'ADMIN')")
    public ResponseEntity<?> cancelPackageChange(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {
        
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        if (user.getPendingPackageType() == null) {
            return ResponseEntity.badRequest().body("No pending package change to cancel.");
        }

        user.setPendingPackageType(null);
        user.setPackageChangeEffectiveDate(null);
        userRepository.save(user);

        actionLogService.logAction(user, ActionType.PACKAGE_CHANGE, 
                "Cancelled pending package change", getClientIp(httpRequest));

        log.info("User {} cancelled package change", user.getUsername());
        return ResponseEntity.ok().body("Package change cancelled.");
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .packageType(user.getPackageType())
                .packageUsage(user.getPackageUsage())
                .pendingPackageType(user.getPendingPackageType())
                .packageChangeEffectiveDate(user.getPackageChangeEffectiveDate())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
