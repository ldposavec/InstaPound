package hr.algebra.nrako.instapound.controller;

import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.service.interfaces.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private UserService userService;

    @GetMapping("/profile/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.getByUsername(username);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("create")
    public ResponseEntity<User> createUser() {
        User user = new User().builder()
                .username("test")
                .email("test@testing.com")
                .password("123")
                .build();
        User savedUser = userService.save(user);
        return ResponseEntity.ok(savedUser);
    }
}
