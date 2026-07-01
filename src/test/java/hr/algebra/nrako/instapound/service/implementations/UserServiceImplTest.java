package hr.algebra.nrako.instapound.service.implementations;

import hr.algebra.nrako.instapound.enums.PackageType;
import hr.algebra.nrako.instapound.enums.UserRole;
import hr.algebra.nrako.instapound.model.dto.response.UserResponse;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.mappers.UserMapper;
import hr.algebra.nrako.instapound.model.valueobject.PackageUsage;
import hr.algebra.nrako.instapound.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .role(UserRole.REGISTERED)
                .packageType(PackageType.FREE)
                .packageUsage(new PackageUsage())
                .createdAt(LocalDateTime.now())
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .role(UserRole.REGISTERED)
                .packageType(PackageType.FREE)
                .packageUsage(new PackageUsage())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void save_shouldPersistUserAndReturnDto() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userResponse);

        UserResponse result = userService.save(userResponse);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void save_shouldMapAllFieldsCorrectly() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userResponse);

        UserResponse result = userService.save(userResponse);

        assertEquals(userResponse.getEmail(), result.getEmail());
        assertEquals(userResponse.getUsername(), result.getUsername());
        assertEquals(userResponse.getRole(), result.getRole());
        assertEquals(userResponse.getPackageType(), result.getPackageType());
    }

    @Test
    void getById_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userResponse);

        Optional<UserResponse> result = userService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void getById_shouldReturnEmptyOptionalWhenUserIsNotPresent() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<UserResponse> result = userService.getById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void getByEmail_shouldReturnUser() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userResponse);

        Optional<UserResponse> result = userService.getByEmail("test@test.com");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void getByEmail_shouldReturnEmptyOptionalWhenUserIsNotPresent() {
        when(userRepository.findByEmail("no@mail.com")).thenReturn(null);

        Optional<UserResponse> result = userService.getByEmail("no@mail.com");

        assertFalse(result.isPresent());
    }

    @Test
    void getByUsername_shouldReturnUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userResponse);

        Optional<UserResponse> result = userService.getByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void getByUsername_shouldReturnEmptyOptionalWhenUserIsNotPresent() {
        when(userRepository.findByUsername("testuser")).thenReturn(null);

        Optional<UserResponse> result = userService.getByUsername("testuser");

        assertFalse(result.isPresent());
    }
}
