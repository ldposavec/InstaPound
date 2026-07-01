package hr.algebra.nrako.instapound.service.implementations;

import hr.algebra.nrako.instapound.enums.ActionType;
import hr.algebra.nrako.instapound.model.dto.response.ActionLogResponse;
import hr.algebra.nrako.instapound.model.entity.ActionLog;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.repository.ActionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ActionLogServiceImplTest {

    @Mock
    private ActionLogRepository actionLogRepository;

    @InjectMocks
    private ActionLogServiceImpl actionLogService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .build();
    }

    @Test
    void logAction_shouldPersistActionLogWithUser() {
        actionLogService.logAction(user, ActionType.USER_LOGIN, "User logged in", "192.168.0.23");

        ArgumentCaptor<ActionLog> captor = ArgumentCaptor.forClass(ActionLog.class);
        verify(actionLogRepository).save(captor.capture());

        ActionLog saved = captor.getValue();
        assertEquals("testuser", saved.getUsername());
        assertEquals(ActionType.USER_LOGIN, saved.getActionType());
        assertEquals("User logged in", saved.getDetails());
        assertEquals("192.168.0.23", saved.getIpAddress());
    }

    @Test
    void logAction_shouldUseAnonymousWhenUserIsNull() {
        actionLogService.logAction(null, ActionType.USER_SEARCH, "Anonymous search", "192.168.0.23");

        ArgumentCaptor<ActionLog> captor = ArgumentCaptor.forClass(ActionLog.class);
        verify(actionLogRepository).save(captor.capture());

        assertEquals("anonymous", captor.getValue().getUsername());
    }

    @Test
    void logAction_shouldSetTimestamp() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        actionLogService.logAction(user, ActionType.PHOTO_UPLOAD, "Upload", null);

        ArgumentCaptor<ActionLog> captor = ArgumentCaptor.forClass(ActionLog.class);
        verify(actionLogRepository).save(captor.capture());

        assertNotNull(captor.getValue().getTimestamp());
        assertTrue(captor.getValue().getTimestamp().isAfter(before));
    }

    @Test
    void logActionWithTargetPhoto_shouldIncludeTargetPhotoId() {
        actionLogService.logActionWithTargetPhoto(user, ActionType.PHOTO_VIEW, "Viewed photo", "192.168.0.23", 42L);

        ArgumentCaptor<ActionLog> captor = ArgumentCaptor.forClass(ActionLog.class);
        verify(actionLogRepository).save(captor.capture());

        assertEquals(42L, captor.getValue().getTargetPhotoId());
        assertEquals(ActionType.PHOTO_VIEW, captor.getValue().getActionType());
    }

    @Test
    void logActionWithTargetPhoto_shouldHandleNullUser() {
        actionLogService.logActionWithTargetPhoto(null, ActionType.PHOTO_DOWNLOAD, "Downloaded photo", "192.168.0.23", 10L);

        ArgumentCaptor<ActionLog> captor = ArgumentCaptor.forClass(ActionLog.class);
        verify(actionLogRepository).save(captor.capture());

        assertEquals("anonymous", captor.getValue().getUsername());
        assertEquals(10L, captor.getValue().getTargetPhotoId());
    }
    
    @Test
    void logActionWithTargetUser_shouldIncludeTargetUserId() {
        actionLogService.logActionWithTargetUser(user, ActionType.USER_FOLLOW, "Followed user", "192.168.0.23", 99L);

        ArgumentCaptor<ActionLog> captor = ArgumentCaptor.forClass(ActionLog.class);
        verify(actionLogRepository).save(captor.capture());

        assertEquals(99L, captor.getValue().getTargetUserId());
        assertEquals(ActionType.USER_FOLLOW, captor.getValue().getActionType());
    }
    
    @Test
    void logAnonymousAction_shouldSetUsernameToAnonymous() {
        actionLogService.logAnonymousAction(ActionType.USER_SEARCH, "Anonymous search", "192.168.0.23");
        
        ArgumentCaptor<ActionLog> captor = ArgumentCaptor.forClass(ActionLog.class);
        verify(actionLogRepository).save(captor.capture());
        
        assertEquals("anonymous", captor.getValue().getUsername());
        assertNull(captor.getValue().getUser());
    }
    
    @Test
    void logAnonymousAction_shouldSetCorrectDetails() {
        actionLogService.logAnonymousAction(ActionType.PHOTO_VIEW, "Anonymous viewed photo", "192.168.0.23");

        ArgumentCaptor<ActionLog> captor = ArgumentCaptor.forClass(ActionLog.class);
        verify(actionLogRepository).save(captor.capture());

        assertEquals("Anonymous viewed photo", captor.getValue().getDetails());
        assertEquals("192.168.0.23", captor.getValue().getIpAddress());
    }

    @Test
    void getLogsForUser_shouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        ActionLog log = ActionLog.builder()
                .id(1L).user(user).username("testuser")
                .actionType(ActionType.USER_LOGIN).details("Login")
                .timestamp(LocalDateTime.now()).build();
        Page<ActionLog> page = new PageImpl<>(List.of(log));

        when(actionLogRepository.findByUserOrderByTimestampDesc(user, pageable)).thenReturn(page);

        Page<ActionLogResponse> result = actionLogService.getLogsForUser(user, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("testuser", result.getContent().getFirst().getUsername());
    }

    @Test
    void getLogsByType_shouldFilterByActionType() {
        Pageable pageable = PageRequest.of(0, 10);
        ActionLog log = ActionLog.builder()
                .id(1L).user(user).username("testuser")
                .actionType(ActionType.PHOTO_UPLOAD).details("Upload")
                .timestamp(LocalDateTime.now()).build();
        Page<ActionLog> page = new PageImpl<>(List.of(log));

        when(actionLogRepository.findByActionTypeOrderByTimestampDesc(ActionType.PHOTO_UPLOAD, pageable)).thenReturn(page);

        Page<ActionLogResponse> result = actionLogService.getLogsByType(ActionType.PHOTO_UPLOAD, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(ActionType.PHOTO_UPLOAD, result.getContent().getFirst().getActionType());
    }

    @Test
    void getActionStatisticsForUser_shouldReturnStatisticsMap() {
        List<Object[]> rawResults = List.of(
                new Object[]{ActionType.PHOTO_DELETE, 5L},
                new Object[]{ActionType.PASSWORD_RESET, 2L}
        );

        when(actionLogRepository.countActionsByTypeForUser(user)).thenReturn(rawResults);

        Map<ActionType, Long> result = actionLogService.getActionStatisticsForUser(user);

        assertEquals(2, result.size());
        assertEquals(5L, result.get(ActionType.PHOTO_DELETE));
        assertEquals(2L, result.get(ActionType.PASSWORD_RESET));
    }

    @Test
    void getActionStatisticsForUser_shouldReturnEmptyMapWhenNoLogs() {
        when(actionLogRepository.countActionsByTypeForUser(user)).thenReturn(List.of());

        Map<ActionType, Long> result = actionLogService.getActionStatisticsForUser(user);

        assertTrue(result.isEmpty(), "Result map should be empty when there are no logs for the user");
    }
}
