package com.sprint.ootd5team.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.ootd5team.base.exception.notification.NotificationNotFoundException;
import com.sprint.ootd5team.domain.notification.entity.Notification;
import com.sprint.ootd5team.domain.notification.enums.NotificationLevel;
import com.sprint.ootd5team.domain.notification.fixture.NotificationFixture;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import com.sprint.ootd5team.domain.notification.fixture.NotificationFixture;
import com.sprint.ootd5team.domain.notification.mapper.NotificationMapper;
import com.sprint.ootd5team.domain.notification.repository.NotificationRepository;
import com.sprint.ootd5team.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 단위 테스트")
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = NotificationFixture.createUser(UUID.randomUUID());
    }

    @Test
    void 조회_결과가_limit_이하이면_hasNext_false_nextCursor_null() {
        // given
        var notifications = NotificationFixture.createTestNotifications(user).subList(0, 2);
        given(notificationRepository.findByUserWithCursor(eq(user.getId()), any(), any(), eq(3),
            eq(Direction.DESC)))
            .willReturn(notifications);
        given(notificationRepository.countByReceiverId(user.getId())).willReturn(2L);
        given(notificationMapper.toDto(any(Notification.class)))
            .willAnswer(inv -> NotificationFixture.toDto(inv.getArgument(0)));

        // when
        var response = notificationService.findAll(user.getId(), null, null, 3, Direction.DESC);

        // then
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.data()).hasSize(2);
        assertThat(response.data().get(0).title()).isEqualTo("알림1");
        assertThat(response.data().get(0).level()).isEqualTo(NotificationLevel.INFO);
    }

    @Test
    void 조회_결과가_limit초과이면_hasNext_true__nextCursor_nextIdAfter_세팅() {
        // given
        var notifications = NotificationFixture.createTestNotifications(user);

        given(notificationRepository.findByUserWithCursor(eq(user.getId()), any(), any(), eq(2),
            eq(Direction.DESC)))
            .willReturn(notifications); // limit+1
        given(notificationRepository.countByReceiverId(user.getId())).willReturn(3L);
        given(notificationMapper.toDto(any(Notification.class)))
            .willAnswer(inv -> NotificationFixture.toDto(inv.getArgument(0)));

        // when
        var response = notificationService.findAll(user.getId(), null, null, 2, Direction.DESC);

        // then
        assertThat(response.hasNext()).isTrue();
        assertThat(response.data()).hasSize(2);
        assertThat(response.nextCursor()).isEqualTo(notifications.get(1).getCreatedAt().toString());
        assertThat(response.nextIdAfter()).isEqualTo(notifications.get(1).getId().toString());
    }

    @Test
    void 본인_알림_삭제_성공() {
        // given
        var notification = NotificationFixture.createNotification(user, "삭제할알림", "내용", Instant.now());
        given(notificationRepository.findById(notification.getId()))
            .willReturn(Optional.of(notification));

        // when
        notificationService.delete(user.getId(), notification.getId());

        // then
        then(notificationRepository).should().delete(notification);
    }

    @Test
    void 존재하지_않는_알림_삭제시_NotificationNotFoundException_발생() {
        // given
        UUID randomId = UUID.randomUUID();
        given(notificationRepository.findById(randomId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.delete(user.getId(), randomId))
            .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    void 본인_알림이_아니면_AccessDeniedException_발생() {
        // given
        User otherUser = NotificationFixture.createUser(UUID.randomUUID());
        var notification = NotificationFixture.createNotification(otherUser, "다른사람알림", "내용", Instant.now());
        given(notificationRepository.findById(notification.getId()))
            .willReturn(Optional.of(notification));

        // when & then
        assertThatThrownBy(() -> notificationService.delete(user.getId(), notification.getId()))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("알림 생성 성공")
    void 알림_생성_성공() {
        // given
        UUID receiverId = user.getId();
        var notification = NotificationFixture.createNotification(
            user, "제목", "내용", Instant.now()
        );
        given(entityManager.getReference(User.class, receiverId))
            .willReturn(user);
        given(notificationRepository.save(any(Notification.class)))
            .willReturn(notification);
        given(notificationMapper.toDto(any(Notification.class)))
            .willReturn(NotificationFixture.toDto(notification));

        // when
        var result = notificationService.createNotification(
            receiverId,
            NotificationTemplateType.ROLE_UPDATED,
            NotificationLevel.INFO,
            "USER", "ADMIN"
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(notification.getTitle());
        assertThat(result.content()).isEqualTo(notification.getContent());
        assertThat(result.level()).isEqualTo(NotificationLevel.INFO);

        then(notificationRepository).should().save(any(Notification.class));
        then(notificationMapper).should().toDto(any(Notification.class));
    }

    @Test
    @DisplayName("존재하지 않는 유저에게 알림 생성 시 EntityNotFoundException 발생")
    void 존재하지않는_유저에게_알림생성시_예외발생() {
        // given
        UUID invalidUserId = UUID.randomUUID();
        given(entityManager.getReference(User.class, invalidUserId))
            .willThrow(new EntityNotFoundException("User not found"));

        // when & then
        assertThatThrownBy(() -> notificationService.createNotification(
            invalidUserId,
            NotificationTemplateType.ROLE_UPDATED,
            NotificationLevel.INFO,
            "USER", "ADMIN"
        )).isInstanceOf(EntityNotFoundException.class);
    }

}
