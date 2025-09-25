package com.sprint.ootd5team.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.ootd5team.base.config.QuerydslConfig;
import com.sprint.ootd5team.domain.notification.entity.Notification;
import com.sprint.ootd5team.domain.notification.repository.NotificationRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Import(QuerydslConfig.class)
@ActiveProfiles("test")
@Sql(scripts = {"classpath:user-data.sql", "classpath:notification-data.sql"})
@TestPropertySource(properties = "spring.sql.init.mode=never")
@DisplayName("NotificationRepositoryImpl 슬라이스 테스트")
class NotificationRepositoryImplTest {

    private static final UUID RECEIVER_ID =
        UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("DESC 정렬 - 최신순 알림 조회")
    void findByUserWithCursor_desc() {
        List<Notification> result = notificationRepository.findByUserWithCursor(
            RECEIVER_ID, null, null, 2, Direction.DESC
        );

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTitle()).isEqualTo("알림4"); // 가장 최신
        assertThat(result.get(1).getTitle()).isEqualTo("알림3");
    }

    @Test
    @DisplayName("ASC 정렬 - 오래된순 알림 조회")
    void findByUserWithCursor_asc() {
        List<Notification> result = notificationRepository.findByUserWithCursor(
            RECEIVER_ID, null, null, 2, Direction.ASC
        );

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTitle()).isEqualTo("알림1"); // 가장 오래된
        assertThat(result.get(1).getTitle()).isEqualTo("알림2");
    }

    @Test
    @DisplayName("DESC + cursor 지정 → cursor 이전 데이터만 조회")
    void findByUserWithCursor_withCursor_desc() {
        Instant cursor = Instant.parse("2024-01-01T09:00:00Z");
        UUID idAfter = UUID.fromString("33333333-3333-3333-3333-333333333333"); // 알림3

        List<Notification> result = notificationRepository.findByUserWithCursor(
            RECEIVER_ID, cursor, idAfter, 10, Direction.DESC
        );

        // 08:00 시각의 알림들만 나와야 함
        assertThat(result).extracting(Notification::getId)
            .containsExactlyInAnyOrder(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222")
            );
    }

    @Test
    @DisplayName("ASC + cursor 지정 → cursor 이후 데이터만 조회")
    void findByUserWithCursor_withCursor_asc() {
        Instant cursor = Instant.parse("2024-01-01T08:00:00Z");
        UUID idAfter = UUID.fromString("11111111-1111-1111-1111-111111111111"); // 알림1

        List<Notification> result = notificationRepository.findByUserWithCursor(
            RECEIVER_ID, cursor, idAfter, 10, Direction.ASC
        );

        // 알림2는 같은 createdAt이지만 idAfter보다 커야 포함됨
        assertThat(result).extracting(Notification::getId)
            .contains(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                UUID.fromString("44444444-4444-4444-4444-444444444444")
            );
    }
}