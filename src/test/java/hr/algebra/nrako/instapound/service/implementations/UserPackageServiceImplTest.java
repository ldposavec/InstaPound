package hr.algebra.nrako.instapound.service.implementations;

import hr.algebra.nrako.instapound.enums.PackageType;
import hr.algebra.nrako.instapound.model.dto.response.PackageInfoResponse;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.entity.UserPackage;
import hr.algebra.nrako.instapound.model.valueobject.PackageLimits;
import hr.algebra.nrako.instapound.model.valueobject.PackageUsage;
import hr.algebra.nrako.instapound.repository.UserPackageRepository;
import hr.algebra.nrako.instapound.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPackageServiceImplTest {

    @Mock
    private UserPackageRepository userPackageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActionLogServiceImpl actionLogService;

    @InjectMocks
    private UserPackageServiceImpl userPackageService;

    private User user;
    private UserPackage userPackage;
    private PackageLimits packageLimits;

    @BeforeEach
    void setUp() {
        packageLimits = new PackageLimits(5_000_000L, 10, 100, BigDecimal.ZERO);
        userPackage = UserPackage.builder()
                .id(1L)
                .packageType(PackageType.FREE)
                .name("Free Package")
                .description("Basic free package")
                .limits(packageLimits)
                .build();
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .packageType(PackageType.FREE)
                .packageUsage(new PackageUsage())
                .build();
    }

    @Test
    void getAllPackages_shouldReturnAllPackages() {
        List<UserPackage> userPackages = List.of(
                UserPackage.builder()
                                .name("basic").build(),
                UserPackage.builder()
                        .name("premium").build()
        );

        when(userPackageRepository.findAll()).thenReturn(userPackages);

        var result = userPackageService.getAllPackages();

        assertEquals(2, result.size());
    }

    @Test
    void getAllPackages_shouldReturnAllPackagesAsDtos() {
        when(userPackageRepository.findAll()).thenReturn(List.of(userPackage));

        List<PackageInfoResponse> result = userPackageService.getAllPackages();

        assertEquals(1, result.size());
        assertEquals("Free Package", result.getFirst().getName());
    }

    @Test
    void getAllPackages_shouldReturnEmptyListWhenNoPackages() {
        when(userPackageRepository.findAll()).thenReturn(List.of());

        List<PackageInfoResponse> result = userPackageService.getAllPackages();

        assertTrue(result.isEmpty(), "Expected empty list when no packages are available");
    }

    @Test
    void getPackageByType_shouldReturnMatchingPackage() {
        when(userPackageRepository.findByPackageType(PackageType.FREE)).thenReturn(userPackage);

        UserPackage result = userPackageService.getPackageByType(PackageType.FREE);

        assertNotNull(result, "Expected a package to be returned for the given type");
        assertEquals(PackageType.FREE, result.getPackageType());
    }

    @Test
    void getPackageByType_shouldReturnNullWhenNotFound() {
        when(userPackageRepository.findByPackageType(PackageType.GOLD)).thenReturn(null);

        UserPackage result = userPackageService.getPackageByType(PackageType.GOLD);

        assertNull(result);
    }

    @Test
    void getPackageInfoForUser_shouldReturnCompletePackageInfo() {
        when(userPackageRepository.findByPackageType(PackageType.FREE)).thenReturn(userPackage);

        PackageInfoResponse result = userPackageService.getPackageInfoForUser(user);

        assertEquals(PackageType.FREE, result.getPackageType());
        assertEquals("Free Package", result.getName());
        assertEquals(packageLimits, result.getLimits());
    }

    @Test
    void getPackageInfoForUser_shouldFallbackWhenPackageNotFound() {
        when(userPackageRepository.findByPackageType(PackageType.FREE)).thenReturn(null);

        PackageInfoResponse result = userPackageService.getPackageInfoForUser(user);

        assertEquals("FREE", result.getName());
        assertNull(result.getLimits());
    }

    @Test
    void canChangePackage_shouldReturnTrueWhenNeverChanged() {
        user.setLastPackageChangeDate(null);

        boolean result = userPackageService.canChangePackage(user);

        assertTrue(result);
    }

    @Test
    void canChangePackage_shouldReturnFalseWhenChangedToday() {
        user.setLastPackageChangeDate(LocalDate.now());

        boolean result = userPackageService.canChangePackage(user);

        assertFalse(result);
    }

    @Test
    void canChangePackage_shouldReturnTrueWhenChangedYesterday() {
        user.setLastPackageChangeDate(LocalDate.now().minusDays(1));

        boolean result = userPackageService.canChangePackage(user);

        assertTrue(result);
    }

    @Test
    void requestPackageChange_shouldReturnFalseWhenCannotChangeToday() {
        user.setLastPackageChangeDate(LocalDate.now());

        boolean result = userPackageService.requestPackageChange(user, PackageType.PRO);

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void requestPackageChange_shouldReturnFalseWhenSamePackage() {
        user.setLastPackageChangeDate(null);

        boolean result = userPackageService.requestPackageChange(user, PackageType.FREE);

        assertFalse(result);
    }

    @Test
    void requestPackageChange_shouldSucceedForValidRequest() {
        user.setLastPackageChangeDate(null);

        boolean result = userPackageService.requestPackageChange(user, PackageType.PRO);

        assertTrue(result);
        assertEquals(PackageType.PRO, user.getPendingPackageType());
        assertEquals(LocalDate.now(), user.getLastPackageChangeDate());
        assertEquals(LocalDate.now().plusDays(1), user.getPackageChangeEffectiveDate());
        verify(userRepository).save(user);
        verify(actionLogService).logAction(eq(user), any(), anyString(), any());
    }

    @Test
    void processPendingPackageChange_shouldApplyPendingChanges() {
        User pendingUser = User.builder()
                .id(2L)
                .username("pendinguser")
                .packageType(PackageType.FREE)
                .pendingPackageType(PackageType.PRO)
                .packageUsage(new PackageUsage())
                .build();

        when(userRepository.findUsersWithPendingPackageChanges()).thenReturn(List.of(pendingUser));

        userPackageService.processPendingPackageChanges();

        assertEquals(PackageType.PRO, pendingUser.getPackageType());
        assertNull(pendingUser.getPendingPackageType());
        assertNull(pendingUser.getPackageChangeEffectiveDate());
        verify(userRepository).save(pendingUser);
    }

    @Test
    void processPendingPackageChange_shouldHandleNoPendingUsers() {
        when(userRepository.findUsersWithPendingPackageChanges()).thenReturn(List.of());

        userPackageService.processPendingPackageChanges();

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void save_shouldPersistAndReturnUserPackage() {
        when(userPackageRepository.save(any(UserPackage.class))).thenReturn(userPackage);

        UserPackage result = userPackageService.save(userPackage);

        assertEquals(userPackage, result);
        verify(userPackageRepository).save(userPackage);
    }
}
