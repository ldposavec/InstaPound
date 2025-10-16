package hr.algebra.nrako.instapound.model.entity;

import hr.algebra.nrako.instapound.enums.ActionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ACTION_LOGS", indexes = {
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_action_type", columnList = "actionType"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
public class ActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType;

    @Column(length = 2000)
    private String details;

    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    private Long targetPhotoId;
    private Long targetUserId;
}

