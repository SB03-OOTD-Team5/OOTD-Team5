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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Validated
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
        UUID receiverId = authService.getCurrentUserId();

        log.info("[NotificationController] 전체 조회 요청 수신: "
                + "ownerId={}, cursor={}, idAfter={}, limit={}, sort={}",
            receiverId, cursor, idAfter, limit, direction);

        NotificationDtoCursorResponse response =
            notificationService.findAll(receiverId, cursor, idAfter, limit, direction);

        log.info(
            "[NotificationController] 전체 조회 응답: size={}, hasNext={}, nextCursor={}, nextIdAfter={}",
            response.data().size(), response.hasNext(),
            response.nextCursor(), response.nextIdAfter());

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
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
