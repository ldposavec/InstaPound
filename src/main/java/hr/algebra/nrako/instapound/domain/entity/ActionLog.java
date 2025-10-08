package hr.algebra.nrako.instapound.domain.entity;

import hr.algebra.nrako.instapound.domain.enums.ActionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Action Log entity
 * Records all user actions in the system for audit trail
 * Adheres to Single Responsibility Principle (SOLID)
 */
@Entity
@Table(name = "action_logs", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_action_type", columnList = "actionType"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;
    
    private String username;  // Denormalized for anonymous users
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType;
    
    @Column(length = 2000)
    private String details;  // Additional action details in JSON or text format
    
    private String ipAddress;
    
    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private Long targetPhotoId;  // Photo involved in the action (if applicable)
    
    private Long targetUserId;   // Target user (for admin actions)
}
