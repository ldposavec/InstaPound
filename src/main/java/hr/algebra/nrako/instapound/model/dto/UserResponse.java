package hr.algebra.nrako.instapound.model.dto;

import hr.algebra.nrako.instapound.enums.PackageType;
import hr.algebra.nrako.instapound.enums.UserRole;
import hr.algebra.nrako.instapound.model.valueobject.PackageUsage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private PackageType packageType;
    private PackageUsage packageUsage;
    private PackageType pendingPackageType;
    private LocalDate packageChangeEffectiveDate;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
