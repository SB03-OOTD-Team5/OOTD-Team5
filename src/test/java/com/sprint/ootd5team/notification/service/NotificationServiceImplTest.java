package com.sprint.ootd5team.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.sprint.ootd5team.domain.notification.entity.Notification;
import com.sprint.ootd5team.domain.notification.enums.NotificationLevel;
import com.sprint.ootd5team.domain.notification.mapper.NotificationMapper;
import com.sprint.ootd5team.domain.notification.respository.NotificationRepository;
import com.sprint.ootd5team.domain.notification.service.NotificationServiceImpl;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.notification.fixture.NotificationFixture;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort.Direction;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 단위 테스트")
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = NotificationFixture.createUser(UUID.randomUUID());
    }

    @Test
    @DisplayName("조회 결과가 limit 이하 → hasNext=false, nextCursor=null")
    void findAll_noHasNext() {
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
    @DisplayName("조회 결과가 limit+1 → hasNext=true, nextCursor/nextIdAfter 세팅")
    void findAll_hasNext() {
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
}
