package com.sprint.ootd5team.domain.notification.repository;

import com.sprint.ootd5team.domain.notification.entity.Notification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID>,
    NotificationRepositoryCustom {

    long countByReceiverId(UUID receiverId);

}
