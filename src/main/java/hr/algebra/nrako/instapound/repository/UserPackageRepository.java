package hr.algebra.nrako.instapound.repository;

import hr.algebra.nrako.instapound.domain.entity.UserPackage;
import hr.algebra.nrako.instapound.domain.enums.PackageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository Pattern (Design Pattern #2)
 * Repository for UserPackage entity
 * Adheres to Dependency Inversion Principle (SOLID)
 */
@Repository
public interface UserPackageRepository extends JpaRepository<UserPackage, Long> {
    
    Optional<UserPackage> findByPackageType(PackageType packageType);
}
