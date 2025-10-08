package hr.algebra.nrako.instapound.repository;

import hr.algebra.nrako.instapound.domain.entity.User;
import hr.algebra.nrako.instapound.domain.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository Pattern (Design Pattern #2)
 * Repository for User entity data access
 * Adheres to Dependency Inversion Principle (SOLID) - depends on abstraction
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByAuthProviderAndProviderId(AuthProvider authProvider, String providerId);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.packageChangeEffectiveDate = CURRENT_DATE")
    java.util.List<User> findUsersWithPendingPackageChanges();
}
