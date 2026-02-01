package hr.algebra.nrako.instapound.service.interfaces;

import hr.algebra.nrako.instapound.model.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> getById(Long id);
    Optional<User> getByEmail(String email);
    Optional<User> getByUsername(String username);
    User save(User user);
}
