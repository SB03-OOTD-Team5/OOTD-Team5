package com.sprint.ootd5team.domain.notification.event.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.domain.notification.event.type.base.DomainEvent;
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

    private final KafkaTemplate<String, DomainEvent<?>> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Async("eventTaskExecutor")
    @TransactionalEventListener
    public void publish(DomainEvent<?> event) {
        try {
            // 문자열로 만들지 않고, 객체를 그대로 전송
            kafkaTemplate.send("ootd.Notifications", event);

            int receiverCount = event.getReceiverIds() == null ? 0 : event.getReceiverIds().size();
            log.info("[Kafka] Published event: type={}, receiverCount={}",
                event.getClass().getSimpleName(), receiverCount);

            // 디버깅용 프리뷰만 직렬화
            log.debug("[Kafka] PayloadPreview={}", objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            log.error("Failed to publish notification event", e);
        }
    }
}
