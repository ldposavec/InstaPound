package hr.algebra.nrako.instapound.controller;

import org.springframework.web.bind.annotation.*;
import hr.algebra.nrako.instapound.enums.ActionType;
import hr.algebra.nrako.instapound.model.dto.request.AdminUserUpdateRequest;
import hr.algebra.nrako.instapound.model.dto.request.PackageCreateRequest;
import hr.algebra.nrako.instapound.model.dto.response.ActionLogResponse;
import hr.algebra.nrako.instapound.model.dto.response.PackageInfoResponse;
import hr.algebra.nrako.instapound.model.dto.response.PhotoResponse;
import hr.algebra.nrako.instapound.model.dto.response.UserResponse;
import hr.algebra.nrako.instapound.model.entity.Photo;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.entity.UserPackage;
import hr.algebra.nrako.instapound.model.mappers.PhotoMapper;
import hr.algebra.nrako.instapound.model.mappers.UserMapper;
import hr.algebra.nrako.instapound.model.valueobject.PackageLimits;
import hr.algebra.nrako.instapound.repository.PhotoRepository;
import hr.algebra.nrako.instapound.repository.UserRepository;
import hr.algebra.nrako.instapound.service.interfaces.ActionLogService;
import hr.algebra.nrako.instapound.service.interfaces.UserPackageService;
import hr.algebra.nrako.instapound.service.storage.StorageServiceImpl;
import hr.algebra.nrako.instapound.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserRepository userRepository;
    private final PhotoRepository photoRepository;
    private final ActionLogService actionLogService;
    private final UserPackageService userPackageService;
    private final StorageServiceImpl storageService;
    private final UserMapper userMapper;
    private final PhotoMapper photoMapper;
    private final IpUtils ipUtils;

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users = userRepository.findAll(pageable);

        return ResponseEntity.ok(users.map(userMapper::toDto));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        return userOpt.map(user -> ResponseEntity.ok(userMapper.toDto(user))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Object> updateUser(
            @PathVariable Long id,
            @RequestBody AdminUserUpdateRequest updateRequest,
            @AuthenticationPrincipal UserDetails adminDetails,
            HttpServletRequest request
            ) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        User user = userOpt.get();
        User admin = userRepository.findByUsername(adminDetails.getUsername());

        if (updateRequest.getUsername() != null && !updateRequest.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(updateRequest.getUsername())) return ResponseEntity.badRequest()
                    .body("Username already taken");
            user.setUsername(updateRequest.getUsername());
        }

        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateRequest.getEmail())) return  ResponseEntity.badRequest()
                    .body("Email already in use");
            user.setEmail(updateRequest.getEmail());
        }

        if (updateRequest.getRole() != null) {
            user.setRole(updateRequest.getRole());
        }

        if (updateRequest.getPackageType() != null) {
            user.setPackageType(updateRequest.getPackageType());
        }

        userRepository.save(user);

        actionLogService.logActionWithTargetUser(admin, ActionType.PROFILE_UPDATE,
                "Admin updated user profile: " + user.getUsername(), ipUtils.getClientIp(request), user.getId());
        log.info("Admin {} updated user {}", adminDetails.getUsername(), user.getUsername());
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Object> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails adminDetails,
            HttpServletRequest request) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        User user = userOpt.get();
        User admin = userRepository.findByUsername(adminDetails.getUsername());

        if (user.getId().equals(admin.getId())) return ResponseEntity.badRequest().body("Cannot delete your own " +
                "account");

        List<Photo> userPhotos = photoRepository.findByUser(user);
        for (Photo photo : userPhotos) {
            try {
                storageService.delete(photo.getStoredFileName(), photo.getStorageType());
                storageService.delete("thumb_" + photo.getStoredFileName(), photo.getStorageType());
            } catch (IOException _) {
                log.warn("Could not delete photo files for user {}", user.getUsername());
            }
        }

        userRepository.delete(user);

        actionLogService.logActionWithTargetUser(admin, ActionType.PROFILE_UPDATE,
                "Admin deleted user: " + user.getUsername(), ipUtils.getClientIp(request), user.getId());
        log.info("Admin {} deleted user {}", admin.getUsername(), user.getUsername());
        return ResponseEntity.ok().body("User deleted successfully");
    }

    @GetMapping("/users/{id}/statistics")
    public ResponseEntity<Object> getUserStatistics(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        User user = userOpt.get();

        Map<ActionType, Long> actionStats = actionLogService.getActionStatisticsForUser(user);
        Long photoCount = photoRepository.countByUser(user);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("userId", user.getId());
        statistics.put("username", user.getUsername());
        statistics.put("photoCount", photoCount);
        statistics.put("packageUsage", user.getPackageUsage());
        statistics.put("actionStatistics", actionStats);
        statistics.put("createdAt", user.getCreatedAt());
        statistics.put("lastLoginAt", user.getLastLoginAt());

        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/logs")
    public ResponseEntity<Page<ActionLogResponse>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return ResponseEntity.ok(actionLogService.getAllLogs(pageable));
    }

    @GetMapping("/logs/user/{userId}")
    public ResponseEntity<Page<ActionLogResponse>> getLogsForUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(actionLogService.getLogsForUser(userOpt.get(), pageable));
    }

    @GetMapping("/logs/type/{actionType}")
    public ResponseEntity<Page<ActionLogResponse>> getLogsByType(
            @PathVariable ActionType actionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(actionLogService.getLogsByType(actionType, pageable));
    }

    @GetMapping("/logs/range")
    public ResponseEntity<Page<ActionLogResponse>> getLogsByDateRange(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        LocalDateTime fromDate =  LocalDateTime.parse(from);
        LocalDateTime toDate = LocalDateTime.parse(to);
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(actionLogService.getLogsByDateRange(fromDate, toDate, pageable));
    }

    @GetMapping("/packages")
    public ResponseEntity<List<PackageInfoResponse>> getAllPackages() {
        return ResponseEntity.ok(userPackageService.getAllPackages());
    }

    @PostMapping("/packages")
    public ResponseEntity<Object> savePackage(
            @RequestBody PackageCreateRequest createRequest,
            @AuthenticationPrincipal UserDetails adminDetails,
            HttpServletRequest request
    ) {
        UserPackage userPackage = UserPackage.builder()
                .packageType(createRequest.getPackageType())
                .name(createRequest.getName())
                .description(createRequest.getDescription())
                .limits(new PackageLimits(
                        createRequest.getMaxUploadSizeInBytes(),
                        createRequest.getDailyUploadLimit(),
                        createRequest.getMaxTotalPhotos(),
                        createRequest.getPrice()
                ))
                .build();

        UserPackage savedPacked = userPackageService.save(userPackage);

        User admin = userRepository.findByUsername(adminDetails.getUsername());
        actionLogService.logAction(admin, ActionType.PACKAGE_CHANGE,
                "Admin created/updated package: " + createRequest.getPackageType(), ipUtils.getClientIp(request));
        log.info("Admin {} saved package {}", admin.getUsername(), savedPacked.getPackageType());
        return ResponseEntity.ok(savedPacked);
    }

    @GetMapping("/photos")
    public ResponseEntity<Page<PhotoResponse>> getAllPhotos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        Page<Photo> photos = photoRepository.findAllByOrderByUploadedAtDesc(pageable);

        return ResponseEntity.ok(photos.map(photoMapper::toDto));
    }

    @DeleteMapping("/photos/{id}")
    public ResponseEntity<Object> deletePhoto(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails adminDetails,
            HttpServletRequest request
    ) {
        Optional<Photo> photoOpt = photoRepository.findById(id);
        if (photoOpt.isEmpty()) return ResponseEntity.notFound().build();
        Photo photo = photoOpt.get();
        User admin =  userRepository.findByUsername(adminDetails.getUsername());

        try {
            storageService.delete(photo.getStoredFileName(), photo.getStorageType());
            storageService.delete("thumb_" + photo.getStoredFileName(), photo.getStorageType());
        } catch (IOException _) {
            log.warn("Could not delete photo files: {}", photo.getStoredFileName());
        }

        photoRepository.delete(photo);

        actionLogService.logActionWithTargetPhoto(admin, ActionType.PHOTO_DELETE, "Admin deleted photo: " + id
                + " (owner : " + photo.getUser().getUsername() + ")", ipUtils.getClientIp(request), id);
        log.info("Admin {} deleted photo {} owned by {}", admin.getUsername(), photo.getStoredFileName(),
                photo.getUser().getUsername());
        return ResponseEntity.ok().body("Photo deleted successfully");
    }
}
