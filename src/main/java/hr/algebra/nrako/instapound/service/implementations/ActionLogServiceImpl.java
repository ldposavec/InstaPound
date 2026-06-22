package hr.algebra.nrako.instapound.service.implementations;

import hr.algebra.nrako.instapound.constants.Consts;
import hr.algebra.nrako.instapound.enums.ActionType;
import hr.algebra.nrako.instapound.model.dto.response.ActionLogResponse;
import hr.algebra.nrako.instapound.model.entity.ActionLog;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.repository.ActionLogRepository;
import hr.algebra.nrako.instapound.service.interfaces.ActionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActionLogServiceImpl implements ActionLogService {
    private final ActionLogRepository actionLogRepository;

    @Override
    public void logAction(User user, ActionType actionType, String details, String ipAddress) {
        ActionLog actionLog = ActionLog.builder()
                .user(user)
                .username(user != null ? user.getUsername() : Consts.ANONYMOUS)
                .actionType(actionType)
                .details(details)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .build();
        actionLogRepository.save(actionLog);
        log.info("Action logged: {} by {} - {}", actionType, user != null ? user.getUsername() : Consts.ANONYMOUS, details);
    }

    @Override
    public void logActionWithTargetPhoto(User user, ActionType actionType, String details, String ipAddress, Long targetPhotoId) {
        ActionLog actionLog = ActionLog.builder()
                .user(user)
                .username(user != null ? user.getUsername() : Consts.ANONYMOUS)
                .actionType(actionType)
                .details(details)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .targetPhotoId(targetPhotoId)
                .build();
        actionLogRepository.save(actionLog);
        log.info("Action logged: {} by {} on photo {} - {}", actionType, user != null ? user.getUsername() :
                Consts.ANONYMOUS, targetPhotoId, details);
    }

    @Override
    public void logActionWithTargetUser(User user, ActionType actionType, String details, String ipAddress, Long targetUserId) {
        ActionLog actionLog = ActionLog.builder()
                .user(user)
                .username(user != null ? user.getUsername() : Consts.ANONYMOUS)
                .actionType(actionType)
                .details(details)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .targetUserId(targetUserId)
                .build();
        actionLogRepository.save(actionLog);
        log.info("Action logged: {} by {} targeting user {} - {}", actionType, user != null ? user.getUsername() :
                Consts.ANONYMOUS, targetUserId, details);
    }

    @Override
    public void logAnonymousAction(ActionType actionType, String details, String ipAddress) {
        ActionLog actionLog = ActionLog.builder()
                .username(Consts.ANONYMOUS)
                .actionType(actionType)
                .details(details)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .build();
        actionLogRepository.save(actionLog);
        log.info("Anonymous logged: {} - {}", actionType, details);
    }

    @Override
    public Page<ActionLogResponse> getLogsForUser(User user, Pageable pageable) {
        return actionLogRepository.findByUserOrderByTimestampDesc(user, pageable).map(this::toDto);
    }


    @Override
    public Page<ActionLogResponse> getLogsByType(ActionType actionType, Pageable pageable) {
        return actionLogRepository.findByActionTypeOrderByTimestampDesc(actionType, pageable).map(this::toDto);
    }

    @Override
    public Page<ActionLogResponse> getLogsByDateRange(LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return actionLogRepository.findByTimestampBetween(from, to, pageable).map(this::toDto);
    }

    @Override
    public Map<ActionType, Long> getActionStatisticsForUser(User user) {
        List<Object[]> results = actionLogRepository.countActionsByTypeForUser(user);
        Map<ActionType, Long> stats = new EnumMap<>(ActionType.class);
        for (Object[] result : results) {
            stats.put((ActionType) result[0], (Long) result[1]);
        }
        return stats;
    }

    @Override
    public Page<ActionLogResponse> getAllLogs(Pageable pageable) {
        return actionLogRepository.findAll(pageable).map(this::toDto);
    }

    private ActionLogResponse toDto(ActionLog actionLog) {
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
