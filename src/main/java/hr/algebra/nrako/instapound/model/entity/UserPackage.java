package hr.algebra.nrako.instapound.model.entity;

import hr.algebra.nrako.instapound.enums.PackageType;
import hr.algebra.nrako.instapound.model.valueobject.PackageLimits;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "USER_PACKAGES")
public class UserPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private PackageType packageType;

    @Column(nullable = false)
    private String name;

    private String description;

    @Embedded
    private PackageLimits limits;
}
