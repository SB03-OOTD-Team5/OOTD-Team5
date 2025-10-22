package com.sprint.ootd5team.domain.notification.event.producer;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.then;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.domain.notification.event.type.base.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaEventPublisher 테스트")
class KafkaEventPublisherTest {

    @Mock
    KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    KafkaEventPublisher publisher;

    @Test
    void 이벤트_발행_성공() throws Exception {
        // given
        DomainEvent<?> event = mock(DomainEvent.class);
        given(objectMapper.writeValueAsString(event)).willReturn("{\"mock\":true}");

        // when
        publisher.publish(event);

        // then
        then(kafkaTemplate).should()
            .send(eq("ootd.Notifications"), anyString());
    }

    @Test
    void 직렬화_실패() throws Exception {
        // given
        DomainEvent<?> event = mock(DomainEvent.class);
        given(objectMapper.writeValueAsString(event))
            .willThrow(new RuntimeException("fail to serialize"));

        // when & then
        assertThatCode(() -> publisher.publish(event))
            .doesNotThrowAnyException();
    }
}
