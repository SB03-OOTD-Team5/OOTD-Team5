package com.sprint.ootd5team.domain.notification.service;

import com.sprint.ootd5team.domain.notification.dto.response.NotificationDto;
import com.sprint.ootd5team.domain.notification.dto.response.NotificationDtoCursorResponse;
import com.sprint.ootd5team.domain.notification.enums.NotificationLevel;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Sort;

public interface NotificationService {

    NotificationDto createNotification(
        UUID receiverId,
        NotificationTemplateType type,
        NotificationLevel level,
        Object... args
    );

    NotificationDtoCursorResponse findAll(
        UUID currentUserId,
        Instant cursor,
        UUID idAfter,
        int limit,
        Sort.Direction direction
    );

    void delete(UUID receiverId, UUID notificationId);
}
