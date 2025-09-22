package com.sprint.ootd5team.domain.notification.service;

import com.sprint.ootd5team.domain.notification.dto.response.NotificationDtoCursorResponse;
import java.time.Instant;
import com.sprint.ootd5team.domain.notification.dto.response.NotificationDto;
import com.sprint.ootd5team.domain.notification.enums.NotificationLevel;
import com.sprint.ootd5team.domain.notification.enums.NotificationType;
import java.util.UUID;
import org.springframework.data.domain.Sort;

public interface NotificationService {

    NotificationDto createByReceiverId(
        UUID receiverId,
        NotificationType type,
        NotificationLevel level,
        Object... args
    );

}
