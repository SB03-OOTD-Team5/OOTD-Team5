package com.sprint.ootd5team.domain.notification.service;

import com.sprint.ootd5team.domain.notification.dto.response.NotificationDto;
import com.sprint.ootd5team.domain.notification.dto.response.NotificationDtoCursorResponse;
import com.sprint.ootd5team.domain.notification.enums.NotificationLevel;
import com.sprint.ootd5team.domain.notification.enums.NotificationType;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Sort;

public interface NotificationService {

    NotificationDto createByReceiverId(
        UUID receiverId,
        NotificationType type,
        NotificationLevel level,
        Object... args
    );

    void notifyAllUsers(NotificationType type, NotificationLevel level, Object... args);

    NotificationDtoCursorResponse getNotifications(
        UUID currentUserId,
        Instant cursor,
        UUID idAfter,
        int limit,
        Sort.Direction direction
    );

    void delete(UUID receiverId, UUID notificationId);
}
