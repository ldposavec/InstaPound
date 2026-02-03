package hr.algebra.nrako.instapound.service.implementations;

import hr.algebra.nrako.instapound.enums.ActionType;
import hr.algebra.nrako.instapound.enums.PackageType;
import hr.algebra.nrako.instapound.model.dto.response.PackageInfoResponse;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.entity.UserPackage;
import hr.algebra.nrako.instapound.repository.UserPackageRepository;
import hr.algebra.nrako.instapound.repository.UserRepository;
import hr.algebra.nrako.instapound.service.interfaces.ActionLogService;
import hr.algebra.nrako.instapound.service.interfaces.UserPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserPackageServiceImpl implements UserPackageService {
    private final UserPackageRepository userPackageRepository;
    private final UserRepository userRepository;
    private final ActionLogService actionLogService;

    @Override
    public List<PackageInfoResponse> getAllPackages() {
        return userPackageRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserPackage getPackageByType(PackageType packageType) {
        return userPackageRepository.findByPackageType(packageType);
    }

    @Override
    public PackageInfoResponse getPackageInfoForUser(User user) {
        UserPackage userPackage = userPackageRepository.findByPackageType(user.getPackageType());

        return PackageInfoResponse.builder()
                .packageType(user.getPackageType())
                .name(userPackage != null ? userPackage.getName() : user.getPackageType().name())
                .description(userPackage != null ? userPackage.getDescription() : null)
                .limits(userPackage != null ? userPackage.getLimits() : null)
                .currentUsage(user.getPackageUsage())
                .pendingPackageType(user.getPendingPackageType())
                .canChangeToday(canChangePackage(user))
                .build();
    }

    @Override
    @Transactional
    public boolean requestPackageChange(User user, PackageType newPackage) {
        if (!canChangePackage(user)) {
            log.warn("User {} cannot change package today", user.getUsername());
            return false;
        }

        if (user.getPackageType() == newPackage) {
            log.warn("User {} already has package {}", user.getUsername(), newPackage);
            return false;
        }

        user.setPendingPackageType(newPackage);
        user.setLastPackageChangeDate(LocalDate.now());
        user.setPackageChangeEffectiveDate(LocalDate.now().plusDays(1));
        userRepository.save(user);

        actionLogService.logAction(user, ActionType.PACKAGE_CHANGE,
                "Requested package change from " + user.getPackageType() + " to " + newPackage, null);
        log.info("User {} requested package change to {}, effective {}", user.getUsername(), newPackage,
                user.getPackageChangeEffectiveDate());
        return true;
    }

    @Override
    public boolean canChangePackage(User user) {
        if (user.getLastPackageChangeDate() == null) return true;
        return !user.getLastPackageChangeDate().equals(LocalDate.now());
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processPendingPackageChanges() {
        List<User> usersWithPendingChanges = userRepository.findUsersWithPendingPackageChanges();

        for (User user : usersWithPendingChanges) {
            if (user.getPackageChangeEffectiveDate() != null && 
                    !LocalDate.now().isBefore(user.getPackageChangeEffectiveDate())) {
                PackageType oldPackage = user.getPackageType();
                user.setPackageType(user.getPendingPackageType());
                user.setPendingPackageType(null);
                user.setPackageChangeEffectiveDate(null);
                userRepository.save(user);

                log.info("Applied package change for user {}: {} -> {}", user.getUsername(), oldPackage, user.getPackageType());
            }
        }
    }

    @Override
    @Transactional
    public UserPackage save(UserPackage userPackage) {
        return userPackageRepository.save(userPackage);
    }

    private PackageInfoResponse toDto(UserPackage userPackage) {
        return PackageInfoResponse.builder()
                .packageType(userPackage.getPackageType())
                .name(userPackage.getName())
                .description(userPackage.getDescription())
                .limits(userPackage.getLimits())
                .build();
    }
}
