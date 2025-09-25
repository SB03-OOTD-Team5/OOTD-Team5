package com.sprint.ootd5team.domain.notification.service;

import com.sprint.ootd5team.domain.notification.dto.response.NotificationDtoCursorResponse;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Sort;

public interface NotificationService {

    NotificationDtoCursorResponse findAll(
        UUID currentUserId,
        Instant cursor,
        UUID idAfter,
        int limit,
        Sort.Direction direction
    );

    void delete(UUID receiverId, UUID notificationId);
}
