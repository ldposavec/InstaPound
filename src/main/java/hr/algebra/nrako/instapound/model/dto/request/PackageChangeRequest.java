package hr.algebra.nrako.instapound.model.dto.request;

import hr.algebra.nrako.instapound.enums.PackageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PackageChangeRequest {
    private PackageType newPackage;
}
