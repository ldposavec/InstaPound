package hr.algebra.nrako.instapound.model.dto;

import hr.algebra.nrako.instapound.enums.PackageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageChangeRequest {
    private PackageType newPackage;
}
