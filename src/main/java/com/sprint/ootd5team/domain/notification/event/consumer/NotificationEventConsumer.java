package com.sprint.ootd5team.domain.notification.event.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
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
            DomainEvent<?> event = objectMapper.readValue(message, DomainEvent.class);
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
}
