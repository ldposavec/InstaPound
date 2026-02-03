package hr.algebra.nrako.instapound.model.dto.response;

import hr.algebra.nrako.instapound.enums.ActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionLogResponse {
    private Long id;
    private Long userId;
    private String username;
    private ActionType actionType;
    private String details;
    private String ipAddress;
    private LocalDateTime timestamp;
    private Long targetPhotoId;
    private Long targetUserId;
}
