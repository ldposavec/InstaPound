package hr.algebra.nrako.instapound.service.interfaces;

import hr.algebra.nrako.instapound.enums.ActionType;
import hr.algebra.nrako.instapound.model.dto.response.ActionLogResponse;
import hr.algebra.nrako.instapound.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Map;

public interface ActionLogService {
    void logAction(User user, ActionType actionType, String details, String ipAddress);
    void logActionWithTargetPhoto(User user, ActionType actionType, String details, String ipAddress, Long targetPhotoId);
    void logActionWithTargetUser(User user, ActionType actionType, String details, String ipAddress, Long targetUserId);
    void logAnonymousAction(ActionType actionType, String details, String ipAddress);
    Page<ActionLogResponse> getLogsForUser(User user, Pageable pageable);
    Page<ActionLogResponse> getLogsByType(ActionType actionType, Pageable pageable);
    Page<ActionLogResponse> getLogsByDateRange(LocalDateTime from, LocalDateTime to, Pageable pageable);
    Map<ActionType, Long> getActionStatisticsForUser(User user);
    Page<ActionLogResponse> getAllLogs(Pageable pageable);
}
