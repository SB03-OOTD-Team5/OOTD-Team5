package com.sprint.ootd5team.domain.notification.controller;

import com.sprint.ootd5team.domain.notification.controller.api.NotificationApi;
import com.sprint.ootd5team.domain.notification.dto.response.NotificationDtoCursorResponse;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController implements NotificationApi {

    @Override
    public ResponseEntity<NotificationDtoCursorResponse> getNotifications(
        Instant cursor,
        UUID idAfter,
        int limit,
        Sort.Direction direction
    ) {
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }

    @Override
    public ResponseEntity<Void> delete(UUID notificationId) {
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }
}
