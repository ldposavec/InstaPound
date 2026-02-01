package hr.algebra.nrako.instapound.service.interfaces;

import hr.algebra.nrako.instapound.enums.ActionType;
import hr.algebra.nrako.instapound.model.dto.ActionLogResponse;
import hr.algebra.nrako.instapound.model.entity.ActionLog;
import hr.algebra.nrako.instapound.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Action Log Service Interface - Observer Pattern
 * Records and retrieves user action logs
 */
public interface ActionLogService {

    /**
     * Log a user action
     */
    void logAction(User user, ActionType actionType, String details, String ipAddress);

    /**
     * Log a user action with target photo
     */
    void logAction(User user, ActionType actionType, String details, String ipAddress, Long targetPhotoId);

    /**
     * Log a user action with target user
     */
    void logActionWithTargetUser(User user, ActionType actionType, String details, String ipAddress, Long targetUserId);

    /**
     * Log anonymous action
     */
    void logAnonymousAction(ActionType actionType, String details, String ipAddress);

    /**
     * Get action logs for a user
     */
    Page<ActionLogResponse> getLogsForUser(User user, Pageable pageable);

    /**
     * Get action logs by type
     */
    Page<ActionLogResponse> getLogsByType(ActionType actionType, Pageable pageable);

    /**
     * Get action logs in date range
     */
    Page<ActionLogResponse> getLogsByDateRange(LocalDateTime from, LocalDateTime to, Pageable pageable);

    /**
     * Get action statistics for a user
     */
    Map<ActionType, Long> getActionStatisticsForUser(User user);

    /**
     * Get all action logs (admin only)
     */
    Page<ActionLogResponse> getAllLogs(Pageable pageable);
}
