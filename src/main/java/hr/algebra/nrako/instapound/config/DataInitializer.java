package hr.algebra.nrako.instapound.config;

import hr.algebra.nrako.instapound.domain.entity.UserPackage;
import hr.algebra.nrako.instapound.domain.enums.PackageType;
import hr.algebra.nrako.instapound.domain.valueobject.PackageLimits;
import hr.algebra.nrako.instapound.repository.UserPackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Data initializer to seed default packages
 * Singleton Pattern (Design Pattern #8) - Spring Component is a singleton by default
 * Adheres to Single Responsibility Principle (SOLID)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserPackageRepository packageRepository;
    
    @Override
    public void run(String... args) {
        initializePackages();
    }
    
    private void initializePackages() {
        if (packageRepository.count() > 0) {
            log.info("Packages already initialized");
            return;
        }
        
        // FREE Package
        UserPackage freePackage = UserPackage.builder()
            .packageType(PackageType.FREE)
            .name("Free")
            .description("Free tier with basic features")
            .limits(new PackageLimits(
                5_000_000L,      // 5MB max upload size
                5,               // 5 uploads per day
                50,              // 50 total photos
                BigDecimal.ZERO  // Free
            ))
            .build();
        
        // PRO Package
        UserPackage proPackage = UserPackage.builder()
            .packageType(PackageType.PRO)
            .name("Pro")
            .description("Professional tier with enhanced limits")
            .limits(new PackageLimits(
                20_000_000L,               // 20MB max upload size
                50,                        // 50 uploads per day
                1000,                      // 1000 total photos
                new BigDecimal("9.99")     // $9.99
            ))
            .build();
        
        // GOLD Package
        UserPackage goldPackage = UserPackage.builder()
            .packageType(PackageType.GOLD)
            .name("Gold")
            .description("Premium tier with maximum features")
            .limits(new PackageLimits(
                100_000_000L,              // 100MB max upload size
                500,                       // 500 uploads per day
                10000,                     // 10000 total photos
                new BigDecimal("29.99")    // $29.99
            ))
            .build();
        
        packageRepository.save(freePackage);
        packageRepository.save(proPackage);
        packageRepository.save(goldPackage);
        
        log.info("Initialized {} packages", 3);
    }
}
