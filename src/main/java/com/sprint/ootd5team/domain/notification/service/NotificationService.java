package com.sprint.ootd5team.domain.notification.service;

import com.sprint.ootd5team.domain.notification.dto.response.NotificationDto;
import com.sprint.ootd5team.domain.notification.enums.NotificationLevel;
import com.sprint.ootd5team.domain.notification.enums.NotificationType;
import java.util.UUID;

public interface NotificationService {

    NotificationDto createByReceiverId(
        UUID receiverId,
        NotificationType type,
        NotificationLevel level,
        Object... args
    );

    void notifyAllUsers(NotificationType type, NotificationLevel level, Object... args);

}
