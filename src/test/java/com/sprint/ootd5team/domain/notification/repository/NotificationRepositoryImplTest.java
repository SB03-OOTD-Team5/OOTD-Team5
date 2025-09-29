package com.sprint.ootd5team.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.ootd5team.base.config.QuerydslConfig;
import com.sprint.ootd5team.domain.notification.entity.Notification;
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
@Sql(scripts = {"classpath:testdata/user-data.sql", "classpath:testdata/notification-data.sql"})
@TestPropertySource(properties = "spring.sql.init.mode=never")
@DisplayName("NotificationRepositoryImpl 슬라이스 테스트")
class NotificationRepositoryImplTest {

    private static final UUID RECEIVER_ID =
        UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void DESC_정렬_최신순_알림_조회() {
        // when
        List<Notification> result = notificationRepository.findByUserWithCursor(
            RECEIVER_ID, null, null, 2, Direction.DESC
        );

        // then
        assertThat(result).hasSize(3);  // +1
        assertThat(result.get(0).getTitle()).isEqualTo("알림4");
        assertThat(result.get(1).getTitle()).isEqualTo("알림3");
    }

    @Test
    void ASC_정렬_오래된순_알림_조회() {
        // when
        List<Notification> result = notificationRepository.findByUserWithCursor(
            RECEIVER_ID, null, null, 2, Direction.ASC
        );

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTitle()).isEqualTo("알림1");
        assertThat(result.get(1).getTitle()).isEqualTo("알림2");
    }

    @Test
    void DESC_정렬시_CURSOR_이전_데이터만_조회() {
        // given
        Instant cursor = Instant.parse("2024-01-01T09:00:00Z");
        UUID idAfter = UUID.fromString("33333333-3333-3333-3333-333333333333");

        // when
        List<Notification> result = notificationRepository.findByUserWithCursor(
            RECEIVER_ID, cursor, idAfter, 10, Direction.DESC
        );

        // then
        assertThat(result).extracting(Notification::getId)
            .containsExactlyInAnyOrder(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222")
            );
    }

    @Test
    void ASC_정렬시_CURSOR_이후_데이터만_조회() {
        // given
        Instant cursor = Instant.parse("2024-01-01T08:00:00Z");
        UUID idAfter = UUID.fromString("11111111-1111-1111-1111-111111111111");

        // when
        List<Notification> result = notificationRepository.findByUserWithCursor(
            RECEIVER_ID, cursor, idAfter, 10, Direction.ASC
        );

        // then
        assertThat(result).extracting(Notification::getId)
            .contains(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                UUID.fromString("44444444-4444-4444-4444-444444444444")
            );
    }
}