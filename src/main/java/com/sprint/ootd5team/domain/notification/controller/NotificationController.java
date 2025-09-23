package com.sprint.ootd5team.domain.notification.controller;

import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.domain.notification.controller.api.NotificationApi;
import com.sprint.ootd5team.domain.notification.dto.response.NotificationDtoCursorResponse;
import com.sprint.ootd5team.domain.notification.service.NotificationService;
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

    private final NotificationService notificationService;
    private final AuthService authService;

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
        UUID currentUserId = authService.getCurrentUserId();

        log.info("[NotificationController] 삭제 요청: , currentUserId={}, notificationId={}", currentUserId, notificationId);

        notificationService.delete(currentUserId, notificationId);

        log.info("[NotificationController] 삭제 응답 완료");
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }
}
