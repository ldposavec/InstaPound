package hr.algebra.nrako.instapound.repository;

import hr.algebra.nrako.instapound.enums.PackageType;
import hr.algebra.nrako.instapound.model.entity.UserPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPackageRepository extends JpaRepository<UserPackage, Long> {
    UserPackage findByPackageType(PackageType packageType);
}
