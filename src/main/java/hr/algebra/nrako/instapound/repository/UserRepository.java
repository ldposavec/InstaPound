package hr.algebra.nrako.instapound.repository;

import hr.algebra.nrako.instapound.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);
    User findByUsername(String username);
    User findByEmail(String email);
    List<User> findAll();
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.packageChangeEffectiveDate = CURRENT_DATE")
    List<User> findUsersWithPendingPackageChanges();
}
