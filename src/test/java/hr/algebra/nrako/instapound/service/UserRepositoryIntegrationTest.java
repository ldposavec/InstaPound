package hr.algebra.nrako.instapound.service;

import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class UserRepositoryIntegrationTest {

    @Autowired
    UserRepository repository;

    @Test
    void testFindUserByUsername() {
        User user = User.builder()
                .username("myuser")
                .build();

        repository.save(user);

        User found = repository.findByUsername("myuser");

        assertNotNull(found);
    }
}
