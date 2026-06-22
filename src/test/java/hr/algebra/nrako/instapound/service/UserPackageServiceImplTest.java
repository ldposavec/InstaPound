package hr.algebra.nrako.instapound.service;

import hr.algebra.nrako.instapound.model.entity.UserPackage;
import hr.algebra.nrako.instapound.repository.UserPackageRepository;
import hr.algebra.nrako.instapound.service.implementations.UserPackageServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPackageServiceImplTest {

    @Mock
    private UserPackageRepository userPackageRepository;

    @InjectMocks
    private UserPackageServiceImpl userPackageService;

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
}
