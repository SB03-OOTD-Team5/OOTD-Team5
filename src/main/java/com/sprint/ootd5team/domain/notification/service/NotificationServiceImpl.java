package com.sprint.ootd5team.domain.notification.service;

import com.sprint.ootd5team.base.exception.notification.NotificationNotFoundException;
import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.domain.notification.dto.response.NotificationDto;
import com.sprint.ootd5team.domain.notification.dto.response.NotificationDtoCursorResponse;
import com.sprint.ootd5team.domain.notification.entity.Notification;
import com.sprint.ootd5team.domain.notification.enums.NotificationLevel;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import com.sprint.ootd5team.domain.notification.mapper.NotificationMapper;
import com.sprint.ootd5team.domain.notification.repository.NotificationRepository;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EntityManager entityManager;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    /**
     * 현재 로그인한 사용자의 알림을 커서 기반 페이지네이션 방식으로 조회
     *
     * @param currentUserId 현재 사용자 UUID
     * @param cursor        조회 기준 시각 (null 가능)
     * @param idAfter       동일 시각일 때 보조 커서로 사용할 알림 UUID (null 가능)
     * @param limit         조회 개수 제한
     * @param direction     정렬 방향 (ASC/DESC)
     * @return 알림 커서 응답 DTO
     */
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

        log.debug(
            "[NotificationService] 알림 조회: userId={}, fetched={}, hasNext={}, cursor={}, idAfter={}",
            currentUserId, notifications.size(), hasNext, cursor, idAfter);

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
     * 특정 사용자에게 알림을 생성
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

    /**
     * 특정 사용자의 알림을 삭제
     *
     * @param receiverId     알림 수신자 UUID
     * @param notificationId 삭제할 알림 UUID
     * @throws NotificationNotFoundException 알림이 존재하지 않을 경우
     * @throws AccessDeniedException         다른 사용자의 알림을 삭제하려고 할 경우
     */
    @Transactional
    @Override
    public void delete(UUID receiverId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> NotificationNotFoundException.withId(notificationId));

        if (!notification.getReceiver().getId().equals(receiverId)) {
            log.warn("[NotificationService] 알림 삭제 거부: receiverId={}, notificationId={}", receiverId,
                notificationId);
            throw new AccessDeniedException("본인 알림만 삭제할 수 있습니다.");
        }

        notificationRepository.delete(notification);
        log.info("[NotificationService] 알림 삭제 성공");
    }

    /* 임시: 날씨 관련 Notification 생성 */
    @Override
    public void createWeatherNotification(UUID profileId, String content) {
        Profile profile = profileRepository.findById(profileId).orElseThrow(
            ProfileNotFoundException::new);
        User user = userRepository.findById(profile.getUser().getId()).orElseThrow(
            UserNotFoundException::new);
        log.info("[NotificationService] 알림 생성. profileId:{}, userId:{}", profileId, user.getId());

        Notification notification = Notification.builder()
            .title("날씨변경타이틀")
            .content(content)
            .level(NotificationLevel.INFO)
            .receiver(user)
            .build();

        notificationRepository.save(notification);
    }
}