package hr.algebra.nrako.instapound.repository;

import hr.algebra.nrako.instapound.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.packageChangeEffectiveDate = CURRENT_DATE AND u.pendingPackageType IS NOT NULL")
    List<User> findUsersWithPendingPackageChanges();
}
