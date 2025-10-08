package hr.algebra.nrako.instapound.repository;

import hr.algebra.nrako.instapound.domain.entity.ActionLog;
import hr.algebra.nrako.instapound.domain.entity.User;
import hr.algebra.nrako.instapound.domain.enums.ActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository Pattern (Design Pattern #2)
 * Repository for ActionLog entity for audit trail
 * Adheres to Dependency Inversion Principle (SOLID)
 */
@Repository
public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {
    
    Page<ActionLog> findByUserOrderByTimestampDesc(User user, Pageable pageable);
    
    Page<ActionLog> findByActionTypeOrderByTimestampDesc(ActionType actionType, Pageable pageable);
    
    @Query("SELECT al FROM ActionLog al WHERE al.timestamp BETWEEN :startDate AND :endDate ORDER BY al.timestamp DESC")
    Page<ActionLog> findByTimestampBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    @Query("SELECT al.actionType, COUNT(al) FROM ActionLog al WHERE al.user = :user GROUP BY al.actionType")
    List<Object[]> countActionsByTypeForUser(@Param("user") User user);
}
