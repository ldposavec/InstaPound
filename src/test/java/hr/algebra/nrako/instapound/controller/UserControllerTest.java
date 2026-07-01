package hr.algebra.nrako.instapound.controller;

import hr.algebra.nrako.instapound.enums.ActionType;
import hr.algebra.nrako.instapound.enums.PackageType;
import hr.algebra.nrako.instapound.enums.UserRole;
import hr.algebra.nrako.instapound.model.dto.request.PackageChangeRequest;
import hr.algebra.nrako.instapound.model.dto.response.PackageInfoResponse;
import hr.algebra.nrako.instapound.model.dto.response.UserResponse;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.mappers.UserMapper;
import hr.algebra.nrako.instapound.repository.UserRepository;
import hr.algebra.nrako.instapound.service.interfaces.ActionLogService;
import hr.algebra.nrako.instapound.service.interfaces.UserPackageService;
import hr.algebra.nrako.instapound.service.interfaces.UserService;
import hr.algebra.nrako.instapound.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock 
    private UserService userService;
    
    @Mock 
    private UserRepository userRepository;
    
    @Mock 
    private UserPackageService userPackageService;
    
    @Mock 
    private ActionLogService actionLogService;
    
    @Mock 
    private UserMapper userMapper;
    
    @Mock 
    private IpUtils ipUtils;

    @Mock 
    private HttpServletRequest request;
    
    @Mock 
    private UserDetails userDetails;

    @InjectMocks 
    private UserController controller;

    @BeforeEach
    void stubCommon() {
        lenient().when(userDetails.getUsername()).thenReturn("testuser");
        lenient().when(ipUtils.getClientIp(any())).thenReturn("192.168.0.23");
    }

    private User buildUser(String username) {
        return User.builder()
                .id(1L)
                .username(username)
                .role(UserRole.REGISTERED)
                .packageType(PackageType.FREE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getCurrentUserProfile_shouldReturnProfile() {
        User user = buildUser("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(UserResponse.builder().id(1L).username("testuser").build());

        ResponseEntity<UserResponse> response = controller.getCurrentUserProfile(userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");
    }

    @Test
    void getCurrentUserProfile_shouldReturnNotFoundWhenUserMissing() {
        when(userRepository.findByUsername("testuser")).thenReturn(null);

        ResponseEntity<UserResponse> response = controller.getCurrentUserProfile(userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getProfileByUsername_shouldReturnProfile() {
        User user = buildUser("bob");
        when(userRepository.findByUsername("bob")).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(UserResponse.builder().id(2L).username("bob").build());

        ResponseEntity<UserResponse> response = controller.getProfileByUsername("bob");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("bob");
    }

    @Test
    void getProfileByUsername_shouldReturnNotFoundWhenMissing() {
        when(userRepository.findByUsername("bob")).thenReturn(null);

        ResponseEntity<UserResponse> response = controller.getProfileByUsername("bob");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getCurrentPackage_shouldReturnPackageInfo() {
        User user = buildUser("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(userPackageService.getPackageInfoForUser(user))
                .thenReturn(PackageInfoResponse.builder().packageType(PackageType.FREE).name("Free").build());

        ResponseEntity<PackageInfoResponse> response = controller.getCurrentPackage(userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPackageType()).isEqualTo(PackageType.FREE);
    }

    @Test
    void getCurrentPackage_shouldReturnNotFoundWhenUserMissing() {
        when(userRepository.findByUsername("testuser")).thenReturn(null);

        ResponseEntity<PackageInfoResponse> response = controller.getCurrentPackage(userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
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
    void requestPackageChange_shouldReturnUnauthorizedWhenUserMissing() {
        when(userRepository.findByUsername("testuser")).thenReturn(null);
        PackageChangeRequest req = new PackageChangeRequest(PackageType.PRO);

        ResponseEntity<Object> response = controller.requestPackageChange(req, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("User not found");
        verify(userPackageService, never()).requestPackageChange(any(), any());
    }

    @Test
    void requestPackageChange_shouldReturnBadRequestWhenDailyLimitReached() {
        User user = buildUser("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(userPackageService.canChangePackage(user)).thenReturn(false);
        PackageChangeRequest req = new PackageChangeRequest(PackageType.PRO);

        ResponseEntity<Object> response = controller.requestPackageChange(req, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .isEqualTo("You can only change your package once per day. Please try again tomorrow.");
        verify(userPackageService, never()).requestPackageChange(any(), any());
    }

    @Test
    void requestPackageChange_shouldReturnOkWhenChangeSucceeds() {
        User user = buildUser("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(userPackageService.canChangePackage(user)).thenReturn(true);
        when(userPackageService.requestPackageChange(eq(user), eq(PackageType.PRO))).thenReturn(true);
        PackageChangeRequest req = new PackageChangeRequest(PackageType.PRO);

        ResponseEntity<Object> response = controller.requestPackageChange(req, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isEqualTo("Package change requested. New package will be effective tomorrow.");
    }

    @Test
    void requestPackageChange_shouldReturnBadRequestWhenServiceRejects() {
        User user = buildUser("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(userPackageService.canChangePackage(user)).thenReturn(true);
        when(userPackageService.requestPackageChange(eq(user), any())).thenReturn(false);
        PackageChangeRequest req = new PackageChangeRequest(PackageType.FREE);

        ResponseEntity<Object> response = controller.requestPackageChange(req, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .isEqualTo("Unable to change package. You may already have this package.");
    }

    @Test
    void cancelPackageChange_shouldReturnUnauthorizedWhenUserMissing() {
        when(userRepository.findByUsername("testuser")).thenReturn(null);

        ResponseEntity<Object> response = controller.cancelPackageChange(userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("User not found");
        verify(userRepository, never()).save(any());
    }

    @Test
    void cancelPackageChange_shouldReturnBadRequestWhenNoPendingChange() {
        User user = buildUser("testuser");
        user.setPendingPackageType(null);
        when(userRepository.findByUsername("testuser")).thenReturn(user);

        ResponseEntity<Object> response = controller.cancelPackageChange(userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("No pending package change to cancel.");
        verify(userRepository, never()).save(any());
    }

    @Test
    void cancelPackageChange_shouldClearPendingChangeAndReturnOk() {
        User user = buildUser("testuser");
        user.setPendingPackageType(PackageType.PRO);
        user.setPackageChangeEffectiveDate(java.time.LocalDate.now());
        when(userRepository.findByUsername("testuser")).thenReturn(user);

        ResponseEntity<Object> response = controller.cancelPackageChange(userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Package change cancelled.");
        assertThat(user.getPendingPackageType()).isNull();
        assertThat(user.getPackageChangeEffectiveDate()).isNull();
        verify(userRepository).save(user);
        verify(actionLogService).logAction(eq(user), eq(ActionType.PACKAGE_CHANGE), any(), any());
    }
}
