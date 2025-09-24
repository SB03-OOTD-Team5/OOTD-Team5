package com.sprint.ootd5team.domain.notification.service;

import com.sprint.ootd5team.domain.notification.dto.response.NotificationDto;
import com.sprint.ootd5team.domain.notification.enums.NotificationLevel;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import java.util.UUID;

public interface NotificationService {

    NotificationDto createNotification(
        UUID receiverId,
        NotificationTemplateType type,
        NotificationLevel level,
        Object... args
    );
}
