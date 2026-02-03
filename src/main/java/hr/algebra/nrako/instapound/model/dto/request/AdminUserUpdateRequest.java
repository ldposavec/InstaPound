package hr.algebra.nrako.instapound.model.dto.request;

import hr.algebra.nrako.instapound.enums.PackageType;
import hr.algebra.nrako.instapound.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserUpdateRequest {
    private String username;
    private String email;
    private UserRole role;
    private PackageType packageType;
}
