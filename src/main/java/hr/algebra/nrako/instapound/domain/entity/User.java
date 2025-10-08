package hr.algebra.nrako.instapound.domain.entity;

import hr.algebra.nrako.instapound.domain.enums.AuthProvider;
import hr.algebra.nrako.instapound.domain.enums.PackageType;
import hr.algebra.nrako.instapound.domain.enums.UserRole;
import hr.algebra.nrako.instapound.domain.valueobject.PackageUsage;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity representing system users
 * Adheres to Single Responsibility Principle (SOLID)
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(unique = true)
    private String username;
    
    @Email
    @Column(unique = true)
    private String email;
    
    private String password;  // Hashed password for local auth
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.ANONYMOUS;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider = AuthProvider.LOCAL;
    
    private String providerId;  // ID from OAuth provider
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PackageType packageType = PackageType.FREE;
    
    @Embedded
    private PackageUsage packageUsage = new PackageUsage();
    
    private LocalDate lastPackageChangeDate;
    
    private LocalDate packageChangeEffectiveDate;  // For next-day package changes
    
    @Enumerated(EnumType.STRING)
    private PackageType pendingPackageType;  // Package to activate on effective date
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime lastLoginAt;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Photo> photos = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<ActionLog> actionLogs = new HashSet<>();
    
    /**
     * Check if user can upload a photo
     * Adheres to Open/Closed Principle (SOLID) - can extend with new validation rules
     */
    public boolean canUpload(long fileSize, UserPackage userPackage) {
        if (packageUsage.getTodayUploadCount() == null) {
            packageUsage.setTodayUploadCount(0);
        }
        
        LocalDate today = LocalDate.now();
        if (!today.equals(packageUsage.getLastUploadDate())) {
            packageUsage.setTodayUploadCount(0);
            packageUsage.setLastUploadDate(today);
        }
        
        return fileSize <= userPackage.getLimits().getMaxUploadSizeBytes() &&
               packageUsage.getTodayUploadCount() < userPackage.getLimits().getDailyUploadLimit() &&
               packageUsage.getCurrentPhotoCount() < userPackage.getLimits().getMaxTotalPhotos();
    }
    
    /**
     * Increment photo upload counters
     */
    public void recordUpload(long fileSize) {
        packageUsage.setCurrentPhotoCount(packageUsage.getCurrentPhotoCount() + 1);
        packageUsage.setTodayUploadCount(packageUsage.getTodayUploadCount() + 1);
        packageUsage.setTotalStorageUsed(packageUsage.getTotalStorageUsed() + fileSize);
        packageUsage.setLastUploadDate(LocalDate.now());
    }
}
