package hr.algebra.nrako.instapound.controller;

import hr.algebra.nrako.instapound.enums.ActionType;
import hr.algebra.nrako.instapound.model.dto.request.PackageChangeRequest;
import hr.algebra.nrako.instapound.model.dto.response.PackageInfoResponse;
import hr.algebra.nrako.instapound.model.dto.response.UserResponse;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.mappers.UserMapper;
import hr.algebra.nrako.instapound.repository.UserRepository;
import hr.algebra.nrako.instapound.service.interfaces.ActionLogService;
import hr.algebra.nrako.instapound.service.interfaces.UserPackageService;
import hr.algebra.nrako.instapound.service.interfaces.UserService;
import hr.algebra.nrako.instapound.utils.AuthUtils;
import hr.algebra.nrako.instapound.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserPackageService userPackageService;
    private final ActionLogService actionLogService;
    private final UserMapper userMapper;
    private final IpUtils ipUtils;
    private final AuthUtils authUtils;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUserProfile(Authentication authentication) {
        User user = authUtils.getUserFromAuthentication(authentication);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @GetMapping("/profile/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getProfileByUsername(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @GetMapping("/package")
    @PreAuthorize("hasAnyRole('REGISTERED', 'ADMIN')")
    public ResponseEntity<PackageInfoResponse> getCurrentPackage(Authentication authentication) {
        User user = authUtils.getUserFromAuthentication(authentication);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(userPackageService.getPackageInfoForUser(user));
    }

    @GetMapping("/packages")
    public ResponseEntity<List<PackageInfoResponse>> getAllPackages() {
        return ResponseEntity.ok(userPackageService.getAllPackages());
    }

    @PostMapping("/package/change")
    @PreAuthorize("hasAnyRole('REGISTERED', 'ADMIN')")
    public ResponseEntity<?> requestPackageChange(
            @RequestBody PackageChangeRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
            ) {
        User user = authUtils.getUserFromAuthentication(authentication);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        if (!userPackageService.canChangePackage(user)) {
            return ResponseEntity.badRequest().body("You can only change your package once per day. Please try again tomorrow.");
        }

        boolean success = userPackageService.requestPackageChange(user, request.getNewPackage());
        if (success) {
            log.info("User {} requested package change to {}",  user.getUsername(), request.getNewPackage());
            return ResponseEntity.ok().body("Package change requested. New package will be effective tomorrow.");
        } else return ResponseEntity.badRequest().body("Unable to change package. You may already have this package.");
    }

    @DeleteMapping("/package/change")
    @PreAuthorize("hasAnyRole('REGISTERED', 'ADMIN')")
    public ResponseEntity<?> cancelPackageChange(Authentication authentication, HttpServletRequest request) {
        User user = authUtils.getUserFromAuthentication(authentication);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        if (user.getPendingPackageType() == null) {
            return ResponseEntity.badRequest().body("No pending package change to cancel.");
        }

        user.setPendingPackageType(null);
        user.setPackageChangeEffectiveDate(null);
        userRepository.save(user);

        actionLogService.logAction(user, ActionType.PACKAGE_CHANGE, "Cancelled pending package change",
                ipUtils.getClientIp(request));
        log.info("User {} cancelled package change", user.getUsername());
        return ResponseEntity.ok().body("Package change cancelled.");
    }
}
