package com.sprint.ootd5team.domain.notification.service;

import com.sprint.ootd5team.domain.notification.dto.response.NotificationDto;
import com.sprint.ootd5team.domain.notification.dto.response.NotificationDtoCursorResponse;
import com.sprint.ootd5team.domain.notification.entity.Notification;
import com.sprint.ootd5team.domain.notification.enums.NotificationLevel;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import com.sprint.ootd5team.domain.notification.mapper.NotificationMapper;
import com.sprint.ootd5team.domain.notification.repository.NotificationRepository;
import com.sprint.ootd5team.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EntityManager entityManager;

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


    /**
     * 특정 사용자에게 알림을 생성하고 SSE로 전송
     *
     * @param receiverId 알림 수신자 UUID
     * @param type       알림 타입
     * @param level      알림 중요도 레벨
     * @param args       알림 메시지 포맷에 사용될 인자
     * @return 생성된 알림 DTO
     */
    @Transactional
    @Override
    public NotificationDto createNotification(UUID receiverId, NotificationTemplateType type,
        NotificationLevel level, Object... args
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
        log.info(
            "[NotificationService] 알림 생성 완료: receiverId={}, type={}, level={}, notificationId={}",
            receiverId, type, level, saved.getId());

        return notificationMapper.toDto(saved);
    }
}
