package hr.algebra.nrako.instapound.controller;

import hr.algebra.nrako.instapound.domain.enums.PackageType;
import hr.algebra.nrako.instapound.dto.UserResponse;
import hr.algebra.nrako.instapound.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user operations
 * Adheres to Single Responsibility Principle (SOLID)
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * Get user profile
     * 
     * @param id User ID
     * @return User response
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        // Implementation would map User to UserResponse
        return ResponseEntity.ok().build();
    }
    
    /**
     * Change user package
     * 
     * @param id User ID
     * @param packageType New package type
     * @return Updated user response
     */
    @PutMapping("/{id}/package")
    public ResponseEntity<UserResponse> changePackage(
            @PathVariable Long id,
            @RequestParam PackageType packageType) {
        UserResponse response = userService.changePackage(id, packageType);
        return ResponseEntity.ok(response);
    }
}
