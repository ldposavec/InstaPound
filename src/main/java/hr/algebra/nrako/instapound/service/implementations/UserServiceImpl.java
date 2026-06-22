package hr.algebra.nrako.instapound.service.implementations;

import hr.algebra.nrako.instapound.model.dto.response.UserResponse;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.mappers.UserMapper;
import hr.algebra.nrako.instapound.repository.UserRepository;
import hr.algebra.nrako.instapound.service.interfaces.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    private UserMapper userMapper;

    @Override
    public UserResponse save(UserResponse user) {
        User savedUser = userRepository.save(toEntity(user));
        return userMapper.toDto(savedUser);
    }

    @Override
    public Optional<UserResponse> getById(Long id) {
        return userRepository.findById(id).map(userMapper::toDto);
    }

    @Override
    public Optional<UserResponse> getByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email)).map(userMapper::toDto);
    }

    @Override
    public Optional<UserResponse> getByUsername(String username) {
        return Optional.ofNullable(userRepository.findByUsername(username)).map(userMapper::toDto);
    }

    private User toEntity(UserResponse user) {
        return User.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .packageType(user.getPackageType())
                .packageUsage(user.getPackageUsage())
                .pendingPackageType(user.getPendingPackageType())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
