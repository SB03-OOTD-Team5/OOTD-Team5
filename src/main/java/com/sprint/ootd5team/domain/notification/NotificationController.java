package com.sprint.ootd5team.domain.notification;

import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.domain.notification.dto.response.NotificationDtoCursorResponse;
import com.sprint.ootd5team.domain.notification.service.NotificationService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<NotificationDtoCursorResponse> getNotifications(
        @RequestParam(required = false) Instant cursor,
        @RequestParam(required = false) UUID idAfter,
        @RequestParam(defaultValue = "20") int limit,
        @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        UUID receiverId = authService.getCurrentUserId();

        log.info("[NotificationController] 전체 조회 요청 수신: "
                + "ownerId={}, cursor={}, idAfter={}, limit={}, sort={}",
            receiverId, cursor, idAfter, limit, direction);

        NotificationDtoCursorResponse response =
            notificationService.getNotifications(receiverId, cursor, idAfter, limit, direction);

        log.info(
            "[NotificationController] 전체 조회 응답: size={}, hasNext={}, nextCursor={}, nextIdAfter={}",
            response.data().size(), response.hasNext(),
            response.nextCursor(), response.nextIdAfter());

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }


}
