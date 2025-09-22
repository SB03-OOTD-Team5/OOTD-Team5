package com.sprint.ootd5team.domain.notification.service;

import com.sprint.ootd5team.base.sse.SseService;
import com.sprint.ootd5team.domain.notification.dto.response.NotificationDto;
import com.sprint.ootd5team.domain.notification.entity.Notification;
import com.sprint.ootd5team.domain.notification.enums.NotificationLevel;
import com.sprint.ootd5team.domain.notification.enums.NotificationType;
import com.sprint.ootd5team.domain.notification.mapper.NotificationMapper;
import com.sprint.ootd5team.domain.notification.respository.NotificationRepository;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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

}
