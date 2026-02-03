package hr.algebra.nrako.instapound.repository;

import hr.algebra.nrako.instapound.enums.ActionType;
import hr.algebra.nrako.instapound.model.entity.ActionLog;
import hr.algebra.nrako.instapound.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {
    Page<ActionLog> findByUserOrderByTimestampDesc(User user, Pageable pageable);

    Page<ActionLog> findByActionTypeOrderByTimestampDesc(ActionType actionType, Pageable pageable);

    Page<ActionLog> findByTimestampBetween(LocalDateTime timestampAfter, LocalDateTime timestampBefore, Pageable pageable);

    @Query("SELECT al.actionType, COUNT(al) FROM ActionLog al WHERE al.user = :user GROUP BY al.actionType")
    List<Object[]> countActionsByTypeForUser(@Param("user") User user);
}
