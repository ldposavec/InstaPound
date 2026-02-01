package hr.algebra.nrako.instapound.service.implementations;

import hr.algebra.nrako.instapound.enums.ActionType;
import hr.algebra.nrako.instapound.model.dto.ActionLogResponse;
import hr.algebra.nrako.instapound.model.entity.ActionLog;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.repository.ActionLogRepository;
import hr.algebra.nrako.instapound.service.interfaces.ActionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Action Log Service Implementation - Observer Pattern
 * Logs all user actions for audit and statistics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActionLogServiceImpl implements ActionLogService {

    private final ActionLogRepository actionLogRepository;

    @Override
    @Transactional
    public void logAction(User user, ActionType actionType, String details, String ipAddress) {
        ActionLog actionLog = ActionLog.builder()
                .user(user)
                .username(user != null ? user.getUsername() : "anonymous")
                .actionType(actionType)
                .details(details)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .build();
        
        actionLogRepository.save(actionLog);
        log.info("Action logged: {} by {} - {}", actionType, 
                user != null ? user.getUsername() : "anonymous", details);
    }

    @Override
    @Transactional
    public void logAction(User user, ActionType actionType, String details, String ipAddress, Long targetPhotoId) {
        ActionLog actionLog = ActionLog.builder()
                .user(user)
                .username(user != null ? user.getUsername() : "anonymous")
                .actionType(actionType)
                .details(details)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .targetPhotoId(targetPhotoId)
                .build();
        
        actionLogRepository.save(actionLog);
        log.info("Action logged: {} by {} on photo {} - {}", actionType, 
                user != null ? user.getUsername() : "anonymous", targetPhotoId, details);
    }

    @Override
    @Transactional
    public void logActionWithTargetUser(User user, ActionType actionType, String details, String ipAddress, Long targetUserId) {
        ActionLog actionLog = ActionLog.builder()
                .user(user)
                .username(user != null ? user.getUsername() : "anonymous")
                .actionType(actionType)
                .details(details)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .targetUserId(targetUserId)
                .build();
        
        actionLogRepository.save(actionLog);
        log.info("Action logged: {} by {} targeting user {} - {}", actionType, 
                user != null ? user.getUsername() : "anonymous", targetUserId, details);
    }

    @Override
    @Transactional
    public void logAnonymousAction(ActionType actionType, String details, String ipAddress) {
        ActionLog actionLog = ActionLog.builder()
                .username("anonymous")
                .actionType(actionType)
                .details(details)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .build();
        
        actionLogRepository.save(actionLog);
        log.info("Anonymous action logged: {} - {}", actionType, details);
    }

    @Override
    public Page<ActionLogResponse> getLogsForUser(User user, Pageable pageable) {
        return actionLogRepository.findByUserOrderByTimestampDesc(user, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<ActionLogResponse> getLogsByType(ActionType actionType, Pageable pageable) {
        return actionLogRepository.findByActionTypeOrderByTimestampDesc(actionType, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<ActionLogResponse> getLogsByDateRange(LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return actionLogRepository.findByTimestampBetween(from, to, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Map<ActionType, Long> getActionStatisticsForUser(User user) {
        List<Object[]> results = actionLogRepository.countActionsByTypeForUser(user);
        Map<ActionType, Long> statistics = new HashMap<>();
        for (Object[] result : results) {
            statistics.put((ActionType) result[0], (Long) result[1]);
        }
        return statistics;
    }

    @Override
    public Page<ActionLogResponse> getAllLogs(Pageable pageable) {
        return actionLogRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    private ActionLogResponse mapToResponse(ActionLog actionLog) {
        return ActionLogResponse.builder()
                .id(actionLog.getId())
                .userId(actionLog.getUser() != null ? actionLog.getUser().getId() : null)
                .username(actionLog.getUsername())
                .actionType(actionLog.getActionType())
                .details(actionLog.getDetails())
                .ipAddress(actionLog.getIpAddress())
                .timestamp(actionLog.getTimestamp())
                .targetPhotoId(actionLog.getTargetPhotoId())
                .targetUserId(actionLog.getTargetUserId())
                .build();
    }
}
