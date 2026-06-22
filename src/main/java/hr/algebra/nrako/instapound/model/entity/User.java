package hr.algebra.nrako.instapound.model.entity;

import jakarta.persistence.*;
import lombok.*;
import hr.algebra.nrako.instapound.enums.AuthProvider;
import hr.algebra.nrako.instapound.enums.PackageType;
import hr.algebra.nrako.instapound.enums.UserRole;
import hr.algebra.nrako.instapound.model.valueobject.PackageUsage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USERS")
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

    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.ANONYMOUS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PackageType packageType = PackageType.FREE;

    @Embedded
    @Builder.Default
    private PackageUsage packageUsage = new PackageUsage();

    private LocalDate lastPackageChangeDate;

    private LocalDate packageChangeEffectiveDate;

    @Enumerated(EnumType.STRING)
    private PackageType pendingPackageType;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastLoginAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private Set<Photo> photos = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private Set<ActionLog> actionLogs = new HashSet<>();

    public Boolean canUpload(Long fileSize, UserPackage userPackage) {

        LocalDate today = LocalDate.now();
        if (!today.equals(packageUsage.getLastUploadDate())) {
            packageUsage.setTodayUploadCount(0);
            packageUsage.setLastUploadDate(today);
        }

        return fileSize <= userPackage.getLimits().getMaxUploadSizeInBytes() &&
                packageUsage.getTodayUploadCount() < userPackage.getLimits().getDailyUploadLimit() &&
                packageUsage.getCurrentPhotoCount() < userPackage.getLimits().getMaxTotalPhotos();
    }

    public void recordUpload(long fileSize) {
        packageUsage.setCurrentPhotoCount(packageUsage.getCurrentPhotoCount() + 1);
        packageUsage.setTodayUploadCount(packageUsage.getTodayUploadCount() + 1);
        packageUsage.setTotalStorageUsed(packageUsage.getTotalStorageUsed() + fileSize);
        packageUsage.setLastUploadDate(LocalDate.now());
    }
}
