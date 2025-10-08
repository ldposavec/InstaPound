package hr.algebra.nrako.instapound.repository;

import hr.algebra.nrako.instapound.domain.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository Pattern (Design Pattern #2)
 * Repository for Hashtag entity
 * Adheres to Dependency Inversion Principle (SOLID)
 */
@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    
    Optional<Hashtag> findByTag(String tag);
    
    boolean existsByTag(String tag);
}
