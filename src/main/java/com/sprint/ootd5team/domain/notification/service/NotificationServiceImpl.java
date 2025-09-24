package com.sprint.ootd5team.domain.notification.service;

import com.sprint.ootd5team.base.sse.service.SseService;
import com.sprint.ootd5team.domain.notification.dto.response.NotificationDto;
import com.sprint.ootd5team.domain.notification.entity.Notification;
import com.sprint.ootd5team.domain.notification.enums.NotificationLevel;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import com.sprint.ootd5team.domain.notification.mapper.NotificationMapper;
import com.sprint.ootd5team.domain.notification.repository.NotificationRepository;
import com.sprint.ootd5team.domain.user.entity.User;
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

    @Transactional
    @Override
    public NotificationDto createByReceiverId(
        UUID receiverId,
        NotificationTemplateType type,
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

}
