package com.sprint.ootd5team.domain.notification.event.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.sprint.ootd5team.base.sse.service.SseService;
import com.sprint.ootd5team.domain.notification.dto.response.NotificationDto;
import com.sprint.ootd5team.domain.notification.event.type.base.DomainEvent;
import com.sprint.ootd5team.domain.notification.service.NotificationService;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final SseService sseService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "ootd.Notifications", groupId = "ootd.notification")
    public void consume(String message) {
        try {
            DomainEvent<?> event;
            try {
                // 1차: 정상(JSON 오브젝트)으로 파싱
                event = objectMapper.readValue(message, DomainEvent.class);
            } catch (MismatchedInputException e) {
                // double-encoded 가능성: 한 번 벗겨서 재시도
                if (looksLikeDoubleEncoded(message)) {
                    String unwrapped = objectMapper.readValue(message, String.class);
                    log.warn("[Kafka] Detected double-encoded JSON. Unwrapped once.");
                    event = objectMapper.readValue(unwrapped, DomainEvent.class);
                } else {
                    throw e;
                }
            }

            log.info("[Kafka] Consumed event: type={}", event.getClass().getSimpleName());
            log.debug("[Kafka] Full message: {}", message);

            List<UUID> receiverIds = event.getReceiverIds();
            if (receiverIds == null || receiverIds.isEmpty()) {
                // 전체 전송
                receiverIds = userRepository.findAllUserIds();
            }

            for (UUID userId : receiverIds) {
                try {
                    createAndSendNotification(userId, event);
                } catch (Exception e) {
                    log.error("[Kafka] Failed to send notification to userId={}", userId, e);
                }
            }

        } catch (Exception ex) {
            log.error("Failed to consume notification message: {}", message, ex);
        }
    }

    private void createAndSendNotification(UUID receiverId, DomainEvent<?> event) {
        NotificationDto dto = notificationService.createNotification(
            receiverId, event.getTemplateType(), event.getLevel(), event.getArgs()
        );
        sseService.send(List.of(receiverId), "notifications", dto);
        log.info("[Kafka] Sent notification: id={}, receiverId={}, type={}",
            dto.id(), receiverId, event.getTemplateType()
        );
        log.debug("[Kafka] Full dto={}", dto);
    }

    private boolean looksLikeDoubleEncoded(String s) {
        if (s == null) return false;
        String trimmed = s.trim();
        // "{" 로 시작하고 "}" 로 끝나지만, 양 끝이 모두 큰따옴표로 감싸져 있으면 이중직렬화 제거
        return trimmed.length() >= 2
            && trimmed.charAt(0) == '"'
            && trimmed.charAt(trimmed.length() - 1) == '"'
            && trimmed.contains("\\\"type\\\"");
    }

}
