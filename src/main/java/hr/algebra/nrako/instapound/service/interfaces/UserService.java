package hr.algebra.nrako.instapound.service.interfaces;

import hr.algebra.nrako.instapound.model.dto.response.UserResponse;

import java.util.Optional;

public interface UserService {
    Optional<UserResponse> getById(Long id);
    Optional<UserResponse> getByEmail(String email);
    Optional<UserResponse> getByUsername(String username);
    UserResponse save(UserResponse user);
//    Optional<UserResponse> update(UserResponse user);
}
