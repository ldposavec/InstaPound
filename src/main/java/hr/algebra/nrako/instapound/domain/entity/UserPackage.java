package hr.algebra.nrako.instapound.domain.entity;

import hr.algebra.nrako.instapound.domain.enums.PackageType;
import hr.algebra.nrako.instapound.domain.valueobject.PackageLimits;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Package/Subscription entity
 * Represents different subscription tiers
 * Adheres to Single Responsibility Principle (SOLID)
 */
@Entity
@Table(name = "user_packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPackage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private PackageType packageType;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Embedded
    private PackageLimits limits;
}
