package hr.algebra.nrako.instapound.model.dto.response;

import hr.algebra.nrako.instapound.enums.PackageType;
import hr.algebra.nrako.instapound.model.valueobject.PackageLimits;
import hr.algebra.nrako.instapound.model.valueobject.PackageUsage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageInfoResponse {
    private PackageType packageType;
    private String name;
    private String description;
    private PackageLimits limits;
    private PackageUsage currentUsage;
    private PackageType pendingPackageType;
    private LocalDate packageChangeEffectiveDate;
    private boolean canChangeToday;
}
