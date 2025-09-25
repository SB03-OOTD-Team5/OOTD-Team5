package com.sprint.ootd5team.domain.notification.fixture;

import com.sprint.ootd5team.domain.notification.dto.response.NotificationDto;
import com.sprint.ootd5team.domain.notification.entity.Notification;
import com.sprint.ootd5team.domain.notification.enums.NotificationLevel;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

public class NotificationFixture {

    public static User createUser(UUID id) {
        User u = new User("test@example.com", "password", "닉네임", Role.USER);
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    public static Notification createNotification(User receiver, String title, String content,
        Instant createdAt) {
        Notification n = Notification.builder()
            .receiver(receiver)
            .title(title)
            .content(content)
            .level(NotificationLevel.INFO)
            .build();
        ReflectionTestUtils.setField(n, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(n, "createdAt", createdAt);
        return n;
    }

    public static NotificationDto toDto(Notification n) {
        return new NotificationDto(
            n.getId(),
            n.getCreatedAt(),
            n.getReceiver().getId(),
            n.getTitle(),
            n.getContent(),
            n.getLevel()
        );
    }

    public static List<Notification> createTestNotifications(User receiver) {
        Instant base = Instant.parse("2024-01-01T10:00:00Z");
        return List.of(
            createNotification(receiver, "알림1", "내용1", base),
            createNotification(receiver, "알림2", "내용2", base.plusSeconds(1)),
            createNotification(receiver, "알림3", "내용3", base.plusSeconds(2))
        );
    }
}