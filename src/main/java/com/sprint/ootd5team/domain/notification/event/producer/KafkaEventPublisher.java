package com.sprint.ootd5team.domain.notification.event.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.domain.notification.event.type.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Async("eventTaskExecutor")
    @TransactionalEventListener
    public void publish(DomainEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("ootd.Notifications", payload);
            log.info("[Kafka] Published event: {}", payload);
        } catch (Exception e) {
            log.error("Failed to publish notification event", e);
        }
    }
}
