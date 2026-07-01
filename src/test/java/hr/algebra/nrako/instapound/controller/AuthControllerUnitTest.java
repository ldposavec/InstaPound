package hr.algebra.nrako.instapound.controller;

import hr.algebra.nrako.instapound.enums.ActionType;
import hr.algebra.nrako.instapound.enums.PackageType;
import hr.algebra.nrako.instapound.enums.StorageType;
import hr.algebra.nrako.instapound.enums.UserRole;
import hr.algebra.nrako.instapound.model.dto.request.AdminUserUpdateRequest;
import hr.algebra.nrako.instapound.model.dto.request.PackageCreateRequest;
import hr.algebra.nrako.instapound.model.dto.response.ActionLogResponse;
import hr.algebra.nrako.instapound.model.dto.response.PackageInfoResponse;
import hr.algebra.nrako.instapound.model.dto.response.PhotoResponse;
import hr.algebra.nrako.instapound.model.dto.response.UserResponse;
import hr.algebra.nrako.instapound.model.entity.Photo;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.entity.UserPackage;
import hr.algebra.nrako.instapound.model.mappers.PhotoMapper;
import hr.algebra.nrako.instapound.model.mappers.UserMapper;
import hr.algebra.nrako.instapound.repository.PhotoRepository;
import hr.algebra.nrako.instapound.repository.UserRepository;
import hr.algebra.nrako.instapound.service.interfaces.ActionLogService;
import hr.algebra.nrako.instapound.service.interfaces.UserPackageService;
import hr.algebra.nrako.instapound.service.storage.StorageServiceImpl;
import hr.algebra.nrako.instapound.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerUnitTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock 
    private PhotoRepository photoRepository;
    
    @Mock 
    private ActionLogService actionLogService;
    
    @Mock 
    private UserPackageService userPackageService;
    
    @Mock 
    private StorageServiceImpl storageService;
    
    @Mock 
    private UserMapper userMapper;
    
    @Mock 
    private PhotoMapper photoMapper;
    
    @Mock 
    private IpUtils ipUtils;
    
    @Mock 
    private HttpServletRequest request;
    
    @Mock 
    private UserDetails adminDetails;

    @InjectMocks
    private AdminController controller;

    @BeforeEach
    void stubCommon() {
        lenient().when(adminDetails.getUsername()).thenReturn("admin");
        lenient().when(ipUtils.getClientIp(any())).thenReturn("192.168.0.23");
    }

    private User buildUser(Long id, String username) {
        return User.builder()
                .id(id)
                .username(username)
                .email(username + "@test.com")
                .role(UserRole.REGISTERED)
                .packageType(PackageType.FREE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Photo buildPhoto(Long id, User owner) {
        return Photo.builder()
                .id(id)
                .user(owner)
                .originalFileName("a.jpg")
                .storedFileName("stored-a.jpg")
                .storageType(StorageType.LOCAL)
                .build();
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        User u = buildUser(1L, "testuser");
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(u), PageRequest.of(0, 10), 1));
        when(userMapper.toDto(u)).thenReturn(UserResponse.builder().id(1L).username("testuser").build());

        ResponseEntity<Page<UserResponse>> response = controller.getAllUsers(0, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().getFirst().getUsername()).isEqualTo("testuser");
    }

    @Test
    void getAllUsers_shouldReturnEmptyPageWhenNoUsers() {
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        ResponseEntity<Page<UserResponse>> response = controller.getAllUsers(0, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
    }

    @Test
    void getUserById_shouldReturnUserWhenFound() {
        User u = buildUser(1L, "testuser");
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(userMapper.toDto(u)).thenReturn(UserResponse.builder().id(1L).username("testuser").build());

        ResponseEntity<UserResponse> response = controller.getUserById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");
    }

    @Test
    void getUserById_shouldReturnNotFoundWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<UserResponse> response = controller.getUserById(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateUser_shouldReturnNotFoundWhenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        AdminUserUpdateRequest req = AdminUserUpdateRequest.builder().username("new").build();

        ResponseEntity<Object> response = controller.updateUser(99L, req, adminDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_shouldReturnBadRequestWhenNewUsernameTaken() {
        User target = buildUser(1L, "testuser");
        User admin = buildUser(2L, "admin");
        when(userRepository.findById(1L)).thenReturn(Optional.of(target));
        when(userRepository.findByUsername("admin")).thenReturn(admin);
        when(userRepository.existsByUsername("taken")).thenReturn(true);
        AdminUserUpdateRequest req = AdminUserUpdateRequest.builder().username("taken").build();

        ResponseEntity<Object> response = controller.updateUser(1L, req, adminDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Username already taken");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_shouldReturnBadRequestWhenNewEmailTaken() {
        User target = buildUser(1L, "testuser");
        User admin = buildUser(2L, "admin");
        when(userRepository.findById(1L)).thenReturn(Optional.of(target));
        when(userRepository.findByUsername("admin")).thenReturn(admin);
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);
        AdminUserUpdateRequest req = AdminUserUpdateRequest.builder().email("taken@test.com").build();

        ResponseEntity<Object> response = controller.updateUser(1L, req, adminDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Email already in use");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_shouldUpdateAllFieldsAndReturnUpdatedUser() {
        User target = buildUser(1L, "testuser");
        User admin = buildUser(2L, "admin");
        when(userRepository.findById(1L)).thenReturn(Optional.of(target));
        when(userRepository.findByUsername("admin")).thenReturn(admin);
        when(userRepository.existsByUsername("newname")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userMapper.toDto(target)).thenReturn(UserResponse.builder().id(1L).username("newname").build());
        AdminUserUpdateRequest req = AdminUserUpdateRequest.builder()
                .username("newname").email("new@test.com").role(UserRole.ADMIN).packageType(PackageType.PRO).build();

        ResponseEntity<Object> response = controller.updateUser(1L, req, adminDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(target.getUsername()).isEqualTo("newname");
        assertThat(target.getEmail()).isEqualTo("new@test.com");
        assertThat(target.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(target.getPackageType()).isEqualTo(PackageType.PRO);
        verify(userRepository).save(target);
        verify(actionLogService).logActionWithTargetUser(eq(admin), eq(ActionType.PROFILE_UPDATE), any(), any(), eq(1L));
    }

    @Test
    void deleteUser_shouldReturnNotFoundWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = controller.deleteUser(99L, adminDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteUser_shouldReturnBadRequestWhenDeletingSelf() {
        User admin = buildUser(1L, "admin");
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findByUsername("admin")).thenReturn(admin);

        ResponseEntity<Object> response = controller.deleteUser(1L, adminDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Cannot delete your own account");
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_shouldDeleteUserAndTheirPhotos() throws Exception {
        User target = buildUser(1L, "testuser");
        User admin = buildUser(2L, "admin");
        Photo p = buildPhoto(10L, target);
        when(userRepository.findById(1L)).thenReturn(Optional.of(target));
        when(userRepository.findByUsername("admin")).thenReturn(admin);
        when(photoRepository.findByUser(target)).thenReturn(Collections.singletonList(p));

        ResponseEntity<Object> response = controller.deleteUser(1L, adminDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("User deleted successfully");
        verify(storageService).delete(eq("stored-a.jpg"), eq(StorageType.LOCAL));
        verify(storageService).delete(eq("thumb_stored-a.jpg"), eq(StorageType.LOCAL));
        verify(userRepository).delete(target);
        verify(actionLogService).logActionWithTargetUser(eq(admin), eq(ActionType.PROFILE_UPDATE), any(), any(), eq(1L));
    }

    @Test
    void deleteUser_shouldContinueWhenPhotoFileDeletionFails() throws Exception {
        User target = buildUser(1L, "testuser");
        User admin = buildUser(2L, "admin");
        Photo p = buildPhoto(10L, target);
        when(userRepository.findById(1L)).thenReturn(Optional.of(target));
        when(userRepository.findByUsername("admin")).thenReturn(admin);
        when(photoRepository.findByUser(target)).thenReturn(Collections.singletonList(p));
        org.mockito.Mockito.doThrow(new IOException("io"))
                .when(storageService).delete(eq("stored-a.jpg"), eq(StorageType.LOCAL));

        ResponseEntity<Object> response = controller.deleteUser(1L, adminDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userRepository).delete(target);
    }

    @Test
    void getUserStatistics_shouldReturnNotFoundWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = controller.getUserStatistics(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getUserStatistics_shouldReturnStatistics() {
        User u = buildUser(1L, "testuser");
        Map<ActionType, Long> stats = new HashMap<>();
        stats.put(ActionType.PHOTO_UPLOAD, 3L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(actionLogService.getActionStatisticsForUser(u)).thenReturn(stats);
        when(photoRepository.countByUser(u)).thenReturn(3L);

        ResponseEntity<Object> response = controller.getUserStatistics(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("username")).isEqualTo("testuser");
        assertThat(body.get("photoCount")).isEqualTo(3L);
        assertThat(body.get("actionStatistics")).isEqualTo(stats);
    }

    @Test
    void getAllLogs_shouldReturnPagedLogs() {
        ActionLogResponse log = ActionLogResponse.builder().id(1L).actionType(ActionType.USER_LOGIN).build();
        when(actionLogService.getAllLogs(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(log), PageRequest.of(0, 20), 1));

        ResponseEntity<Page<ActionLogResponse>> response = controller.getAllLogs(0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().getFirst().getActionType()).isEqualTo(ActionType.USER_LOGIN);
    }

    @Test
    void getLogsForUser_shouldReturnNotFoundWhenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Page<ActionLogResponse>> response = controller.getLogsForUser(99L, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getLogsForUser_shouldReturnPagedLogs() {
        User u = buildUser(1L, "testuser");
        ActionLogResponse log = ActionLogResponse.builder().id(1L).userId(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(actionLogService.getLogsForUser(eq(u), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(log), PageRequest.of(0, 20), 1));

        ResponseEntity<Page<ActionLogResponse>> response = controller.getLogsForUser(1L, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().getFirst().getUserId()).isEqualTo(1L);
    }

    @Test
    void getLogsByType_shouldReturnPagedLogs() {
        ActionLogResponse log = ActionLogResponse.builder().id(1L).actionType(ActionType.PHOTO_UPLOAD).build();
        when(actionLogService.getLogsByType(eq(ActionType.PHOTO_UPLOAD), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(log), PageRequest.of(0, 20), 1));

        ResponseEntity<Page<ActionLogResponse>> response = controller.getLogsByType(ActionType.PHOTO_UPLOAD, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().getFirst().getActionType()).isEqualTo(ActionType.PHOTO_UPLOAD);
    }

    @Test
    void getLogsByDateRange_shouldReturnPagedLogs() {
        ActionLogResponse log = ActionLogResponse.builder().id(1L).build();
        when(actionLogService.getLogsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(log), PageRequest.of(0, 20), 1));

        ResponseEntity<Page<ActionLogResponse>> response =
                controller.getLogsByDateRange("2024-01-01T00:00:00", "2024-12-31T23:59:59", 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void getLogsByDateRange_shouldThrowWhenDateInvalid() {
        org.junit.jupiter.api.Assertions.assertThrows(
                java.time.format.DateTimeParseException.class,
                () -> controller.getLogsByDateRange("not-a-date", "2024-12-31T23:59:59", 0, 20));
    }

    @Test
    void getAllPackages_shouldReturnAllPackages() {
        when(userPackageService.getAllPackages())
                .thenReturn(List.of(PackageInfoResponse.builder().packageType(PackageType.FREE).name("Free").build()));

        ResponseEntity<List<PackageInfoResponse>> response = controller.getAllPackages();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFirst().getName()).isEqualTo("Free");
    }

    @Test
    void savePackage_shouldCreateAndReturnPackage() {
        User admin = buildUser(2L, "admin");
        UserPackage saved = UserPackage.builder().id(1L).packageType(PackageType.PRO).name("Pro").build();
        when(userPackageService.save(any())).thenReturn(saved);
        when(userRepository.findByUsername("admin")).thenReturn(admin);
        PackageCreateRequest req = PackageCreateRequest.builder()
                .packageType(PackageType.PRO).name("Pro").description("desc")
                .maxUploadSizeInBytes(1000L).dailyUploadLimit(10).maxTotalPhotos(100)
                .price(new java.math.BigDecimal("9.99")).build();

        ResponseEntity<Object> response = controller.savePackage(req, adminDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(saved);
        verify(userPackageService).save(any(UserPackage.class));
        verify(actionLogService).logAction(eq(admin), eq(ActionType.PACKAGE_CHANGE), any(), any());
    }

    @Test
    void getAllPhotos_shouldReturnPagedPhotos() {
        User owner = buildUser(1L, "testuser");
        Photo p = buildPhoto(1L, owner);
        when(photoRepository.findAllByOrderByUploadedAtDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(p), PageRequest.of(0, 15), 1));
        when(photoMapper.toDto(p)).thenReturn(PhotoResponse.builder().id(1L).originalFileName("a.jpg").build());

        ResponseEntity<Page<PhotoResponse>> response = controller.getAllPhotos(0, 15);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().getFirst().getOriginalFileName()).isEqualTo("a.jpg");
    }

    @Test
    void deletePhoto_shouldReturnNotFoundWhenMissing() {
        when(photoRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = controller.deletePhoto(99L, adminDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deletePhoto_shouldDeletePhotoAndLogAction() throws Exception {
        User owner = buildUser(1L, "testuser");
        User admin = buildUser(2L, "admin");
        Photo p = buildPhoto(10L, owner);
        when(photoRepository.findById(10L)).thenReturn(Optional.of(p));
        when(userRepository.findByUsername("admin")).thenReturn(admin);

        ResponseEntity<Object> response = controller.deletePhoto(10L, adminDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Photo deleted successfully");
        verify(storageService).delete(eq("stored-a.jpg"), eq(StorageType.LOCAL));
        verify(storageService).delete(eq("thumb_stored-a.jpg"), eq(StorageType.LOCAL));
        verify(photoRepository).delete(p);
        verify(actionLogService).logActionWithTargetPhoto(eq(admin), eq(ActionType.PHOTO_DELETE), any(), any(), eq(10L));
    }

    @Test
    void deletePhoto_shouldStillDeleteWhenFileRemovalFails() throws Exception {
        User owner = buildUser(1L, "testuser");
        User admin = buildUser(2L, "admin");
        Photo p = buildPhoto(10L, owner);
        when(photoRepository.findById(10L)).thenReturn(Optional.of(p));
        when(userRepository.findByUsername("admin")).thenReturn(admin);
        org.mockito.Mockito.doThrow(new IOException("io"))
                .when(storageService).delete(eq("stored-a.jpg"), eq(StorageType.LOCAL));

        ResponseEntity<Object> response = controller.deletePhoto(10L, adminDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(photoRepository).delete(p);
    }
}
