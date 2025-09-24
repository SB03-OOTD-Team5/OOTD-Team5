package com.sprint.ootd5team.domain.notification.respository;

import com.sprint.ootd5team.domain.notification.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;

public interface NotificationRepositoryCustom {

    List<Notification> findByUserWithCursor(
        UUID userId,
        Instant cursor,
        UUID idAfter,
        int size,
        Sort.Direction direction);
}
