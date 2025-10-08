package hr.algebra.nrako.instapound.service;

import hr.algebra.nrako.instapound.domain.entity.ActionLog;
import hr.algebra.nrako.instapound.domain.entity.User;
import hr.algebra.nrako.instapound.domain.enums.ActionType;
import hr.algebra.nrako.instapound.repository.ActionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Observer Pattern (Design Pattern #6) - Observer
 * Service for logging user actions
 * Adheres to Single Responsibility Principle (SOLID)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActionLogger {
    
    private final ActionLogRepository actionLogRepository;
    
    /**
     * Log an action performed by a user
     * 
     * @param user User who performed the action
     * @param actionType Type of action
     * @param details Additional details
     */
    @Transactional
    public void logAction(User user, ActionType actionType, String details) {
        logAction(user, actionType, details, null, null, null);
    }
    
    /**
     * Log an action with full details
     * 
     * @param user User who performed the action
     * @param actionType Type of action
     * @param details Additional details
     * @param ipAddress IP address
     * @param targetPhotoId Target photo ID (if applicable)
     * @param targetUserId Target user ID (if applicable)
     */
    @Transactional
    public void logAction(User user, ActionType actionType, String details, 
                         String ipAddress, Long targetPhotoId, Long targetUserId) {
        ActionLog actionLog = ActionLog.builder()
            .user(user)
            .username(user != null ? user.getUsername() : "anonymous")
            .actionType(actionType)
            .details(details)
            .ipAddress(ipAddress)
            .targetPhotoId(targetPhotoId)
            .targetUserId(targetUserId)
            .build();
        
        actionLogRepository.save(actionLog);
        log.info("Action logged: {} by user {} (ID: {})", actionType, 
            user != null ? user.getUsername() : "anonymous", 
            user != null ? user.getId() : "N/A");
    }
    
    /**
     * Log an anonymous action
     * 
     * @param actionType Type of action
     * @param details Additional details
     * @param ipAddress IP address
     */
    @Transactional
    public void logAnonymousAction(ActionType actionType, String details, String ipAddress) {
        ActionLog actionLog = ActionLog.builder()
            .username("anonymous")
            .actionType(actionType)
            .details(details)
            .ipAddress(ipAddress)
            .build();
        
        actionLogRepository.save(actionLog);
        log.info("Anonymous action logged: {}", actionType);
    }
}
