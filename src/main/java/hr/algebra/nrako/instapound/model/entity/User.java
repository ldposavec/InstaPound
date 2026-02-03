package hr.algebra.nrako.instapound.model.entity;

import hr.algebra.nrako.instapound.enums.AuthProvider;
import hr.algebra.nrako.instapound.enums.ImageFormat;
import hr.algebra.nrako.instapound.enums.PackageType;
import hr.algebra.nrako.instapound.enums.UserRole;
import hr.algebra.nrako.instapound.model.valueobject.PackageUsage;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

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

//    @ManyToMany(fetch = FetchType.EAGER)
//    @JoinTable(
//            name = "USER_ROLES",
//            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
//            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
//    )
//    private Set<UserRole> userRoles;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.ANONYMOUS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PackageType packageType = PackageType.FREE;

    @Embedded
    private PackageUsage packageUsage = new PackageUsage();

    private LocalDate lastPackageChangeDate;

    private LocalDate packageChangeEffectiveDate;

    @Enumerated(EnumType.STRING)
    private PackageType pendingPackageType;

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

    public Boolean canUpload(Long fileSize, UserPackage userPackage) {
        if (packageUsage.getTodayUploadCount() == null) packageUsage.setTodayUploadCount(0);

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
