package com.sprint.ootd5team.domain.notification.service;

import com.sprint.ootd5team.domain.notification.dto.response.NotificationDtoCursorResponse;
import com.sprint.ootd5team.domain.notification.entity.Notification;
import com.sprint.ootd5team.domain.notification.mapper.NotificationMapper;
import com.sprint.ootd5team.domain.notification.repository.NotificationRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    @Override
    public NotificationDtoCursorResponse findAll(UUID currentUserId, Instant cursor,
        UUID idAfter, int limit, Direction direction) {
        List<Notification> notifications = notificationRepository.findByUserWithCursor(
            currentUserId, cursor, idAfter, limit, direction);

        boolean hasNext = notifications.size() > limit;
        if (hasNext) {
            notifications = notifications.subList(0, limit);
        }

        String nextCursor =
            hasNext ? notifications.get(notifications.size() - 1).getCreatedAt().toString() : null;
        String nextIdAfter =
            hasNext ? notifications.get(notifications.size() - 1).getId().toString() : null;

        return new NotificationDtoCursorResponse(
            notifications.stream().map(notificationMapper::toDto).toList(),
            nextCursor,
            nextIdAfter,
            hasNext,
            notificationRepository.countByReceiverId(currentUserId),
            "createdAt",
            direction.name()
        );
    }
}
