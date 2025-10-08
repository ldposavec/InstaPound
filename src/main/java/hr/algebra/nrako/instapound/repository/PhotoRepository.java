package hr.algebra.nrako.instapound.repository;

import hr.algebra.nrako.instapound.domain.entity.Photo;
import hr.algebra.nrako.instapound.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository Pattern (Design Pattern #2)
 * Repository for Photo entity with advanced search capabilities
 * Adheres to Dependency Inversion Principle (SOLID)
 */
@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long>, JpaSpecificationExecutor<Photo> {
    
    Page<Photo> findByUserOrderByUploadedAtDesc(User user, Pageable pageable);
    
    Page<Photo> findAllByOrderByUploadedAtDesc(Pageable pageable);
    
    @Query("SELECT p FROM Photo p WHERE p.uploadedAt BETWEEN :startDate AND :endDate ORDER BY p.uploadedAt DESC")
    Page<Photo> findByUploadedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    @Query("SELECT p FROM Photo p JOIN p.hashtags h WHERE h.tag IN :tags ORDER BY p.uploadedAt DESC")
    Page<Photo> findByHashtagsIn(@Param("tags") List<String> tags, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Photo p WHERE p.user = :user")
    Long countByUser(@Param("user") User user);
}
