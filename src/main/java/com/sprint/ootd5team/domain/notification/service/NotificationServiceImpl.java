package com.sprint.ootd5team.domain.notification.service;

import com.sprint.ootd5team.base.sse.service.SseService;
import com.sprint.ootd5team.domain.notification.dto.response.NotificationDto;
import com.sprint.ootd5team.domain.notification.dto.response.NotificationDtoCursorResponse;
import com.sprint.ootd5team.domain.notification.entity.Notification;
import com.sprint.ootd5team.domain.notification.enums.NotificationLevel;
import com.sprint.ootd5team.domain.notification.enums.NotificationType;
import com.sprint.ootd5team.domain.notification.mapper.NotificationMapper;
import com.sprint.ootd5team.domain.notification.respository.NotificationRepository;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
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
    private final EntityManager entityManager;
    private final SseService sseService;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public NotificationDto createByReceiverId(
        UUID receiverId,
        NotificationType type,
        NotificationLevel level,
        Object... args
    ) {

        String title = type.formatTitle(args);
        String content = type.formatContent(args);

        User receiver = entityManager.getReference(User.class, receiverId);
        Notification notification = Notification.builder()
            .receiver(receiver)
            .title(title)
            .content(content)
            .level(level)
            .build();

        Notification saved = notificationRepository.save(notification);
        NotificationDto dto = notificationMapper.toDto(saved);

        // SSE 전송
        sseService.send(List.of(receiverId), "notifications", dto);

        return dto;
    }

    @Override
    public void notifyAllUsers(NotificationType type, NotificationLevel level, Object... args) {
        String title = type.formatTitle(args);
        String content = type.formatContent(args);

        List<UUID> userIds = userRepository.findAllUserIds();
        for (UUID userId : userIds) {
            Notification notification = Notification.builder()
                .receiver(entityManager.getReference(User.class, userId))
                .title(title)
                .content(content)
                .level(level)
                .build();

            Notification saved = notificationRepository.save(notification);
            NotificationDto dto = notificationMapper.toDto(saved);

            sseService.send(List.of(userId), "notifications", dto);
        }
    }


    @Transactional(readOnly = true)
    @Override
    public NotificationDtoCursorResponse getNotifications(UUID currentUserId, Instant cursor,
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
