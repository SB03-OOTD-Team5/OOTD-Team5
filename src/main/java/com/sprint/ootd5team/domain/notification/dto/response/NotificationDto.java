package com.sprint.ootd5team.domain.notification.dto.response;

import com.sprint.ootd5team.domain.notification.enums.NotificationLevel;
import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    Instant createdAt,
    UUID receiverId,
    String title,
    String content,
    NotificationLevel level

) {

}
