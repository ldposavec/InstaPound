package hr.algebra.nrako.instapound.service;

import hr.algebra.nrako.instapound.domain.entity.User;
import hr.algebra.nrako.instapound.domain.entity.UserPackage;
import hr.algebra.nrako.instapound.domain.enums.ActionType;
import hr.algebra.nrako.instapound.domain.enums.AuthProvider;
import hr.algebra.nrako.instapound.domain.enums.PackageType;
import hr.algebra.nrako.instapound.domain.enums.UserRole;
import hr.algebra.nrako.instapound.dto.UserRegistrationRequest;
import hr.algebra.nrako.instapound.dto.UserResponse;
import hr.algebra.nrako.instapound.exception.PackageLimitExceededException;
import hr.algebra.nrako.instapound.exception.ResourceNotFoundException;
import hr.algebra.nrako.instapound.repository.UserPackageRepository;
import hr.algebra.nrako.instapound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for user management
 * Adheres to Single Responsibility Principle (SOLID)
 * Implements business logic for user operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final UserPackageRepository packageRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActionLogger actionLogger;
    
    /**
     * Register a new user with local authentication
     * 
     * @param request Registration request
     * @return Created user response
     */
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(UserRole.REGISTERED)
            .authProvider(AuthProvider.LOCAL)
            .packageType(request.getPackageType())
            .build();
        
        user = userRepository.save(user);
        actionLogger.logAction(user, ActionType.USER_REGISTER, 
            "User registered with package: " + request.getPackageType());
        
        log.info("User registered: {}", user.getUsername());
        return mapToResponse(user);
    }
    
    /**
     * Find user by ID
     * 
     * @param userId User ID
     * @return User entity
     */
    public User findById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
    
    /**
     * Find user by username
     * 
     * @param username Username
     * @return User entity
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }
    
    /**
     * Change user package
     * Can only be done once per day, effective from the next day
     * 
     * @param userId User ID
     * @param newPackageType New package type
     * @return Updated user response
     */
    @Transactional
    public UserResponse changePackage(Long userId, PackageType newPackageType) {
        User user = findById(userId);
        
        LocalDate today = LocalDate.now();
        
        // Check if user has already changed package today
        if (user.getLastPackageChangeDate() != null && 
            user.getLastPackageChangeDate().equals(today)) {
            throw new PackageLimitExceededException(
                "Package can only be changed once per day. Your change will be effective from: " +
                user.getPackageChangeEffectiveDate());
        }
        
        // Set pending package change
        user.setPendingPackageType(newPackageType);
        user.setLastPackageChangeDate(today);
        user.setPackageChangeEffectiveDate(today.plusDays(1));
        
        user = userRepository.save(user);
        actionLogger.logAction(user, ActionType.PACKAGE_CHANGE, 
            "Package change scheduled from " + user.getPackageType() + " to " + 
            newPackageType + ", effective from " + user.getPackageChangeEffectiveDate());
        
        log.info("Package change scheduled for user {}: {} -> {}, effective from {}", 
            user.getUsername(), user.getPackageType(), newPackageType, 
            user.getPackageChangeEffectiveDate());
        
        return mapToResponse(user);
    }
    
    /**
     * Process pending package changes
     * Scheduled to run daily at midnight
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processPendingPackageChanges() {
        List<User> users = userRepository.findUsersWithPendingPackageChanges();
        
        for (User user : users) {
            user.setPackageType(user.getPendingPackageType());
            user.setPendingPackageType(null);
            user.setPackageChangeEffectiveDate(null);
            
            userRepository.save(user);
            actionLogger.logAction(user, ActionType.PACKAGE_CHANGE, 
                "Package changed to " + user.getPackageType());
            
            log.info("Package changed for user {}: {}", user.getUsername(), user.getPackageType());
        }
        
        log.info("Processed {} pending package changes", users.size());
    }
    
    /**
     * Update user's last login timestamp
     * 
     * @param userId User ID
     */
    @Transactional
    public void updateLastLogin(Long userId) {
        User user = findById(userId);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        actionLogger.logAction(user, ActionType.USER_LOGIN, "User logged in");
    }
    
    /**
     * Get user package limits
     * 
     * @param user User
     * @return UserPackage with limits
     */
    public UserPackage getUserPackage(User user) {
        return packageRepository.findByPackageType(user.getPackageType())
            .orElseThrow(() -> new ResourceNotFoundException("Package", "type", user.getPackageType()));
    }
    
    /**
     * Map User entity to UserResponse DTO
     */
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
}
