package com.sprint.ootd5team.domain.notification.event.consumer;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.anyList;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.sse.service.SseService;
import com.sprint.ootd5team.domain.notification.dto.response.NotificationDto;
import com.sprint.ootd5team.domain.notification.event.type.base.DomainEvent;
import com.sprint.ootd5team.domain.notification.fixture.NotificationFixture;
import com.sprint.ootd5team.domain.notification.service.NotificationService;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventConsumer 테스트")
class NotificationEventConsumerTest {

    @Mock
    NotificationService notificationService;

    @Mock
    UserRepository userRepository;

    @Mock
    SseService sseService;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    NotificationEventConsumer consumer;

    @Test
    void Kafka_메시지를_수신하면_알림_생성_및_SSE_전송을_수행() throws Exception {
        // given
        DomainEvent<?> event = mock(DomainEvent.class);
        UUID receiverId = UUID.randomUUID();
        User receiver = NotificationFixture.createUser(receiverId);
        NotificationDto dto = NotificationFixture.toDto(
            NotificationFixture.createNotification(receiver, "테스트 알림", "테스트 내용", java.time.Instant.now())
        );

        given(objectMapper.readValue(anyString(), eq(DomainEvent.class)))
            .willReturn(event);
        given(event.getReceiverIds()).willReturn(List.of(receiverId));
        given(notificationService.createNotification(any(), any(), any(), any()))
            .willReturn(dto);

        // when
        consumer.consume("{\"mock\":true}");

        // then
        then(notificationService).should(times(1))
            .createNotification(any(), any(), any(), any());
        then(sseService).should(times(1))
            .send(anyList(), eq("notifications"), any(NotificationDto.class));
    }

    @Test
    void 수신자_목록이_없으면_전체_사용자에게_전송() throws Exception {
        // given
        DomainEvent<?> event = mock(DomainEvent.class);
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        List<UUID> allUserIds = List.of(user1, user2);

        User receiver = NotificationFixture.createUser(user1);
        NotificationDto dto = NotificationFixture.toDto(
            NotificationFixture.createNotification(receiver, "전체 전송 알림", "모든 유저에게", Instant.now())
        );

        given(objectMapper.readValue(anyString(), eq(DomainEvent.class)))
            .willReturn(event);
        // 수신자 목록이 비었을 때
        given(event.getReceiverIds()).willReturn(null);
        given(userRepository.findAllUserIds()).willReturn(allUserIds);
        given(notificationService.createNotification(any(), any(), any(), any()))
            .willReturn(dto);

        // when
        consumer.consume("{\"broadcast\":true}");

        // then
        then(userRepository).should(times(1)).findAllUserIds();
        then(notificationService).should(times(2))
            .createNotification(any(), any(), any(), any());
        then(sseService).should(times(2))
            .send(anyList(), eq("notifications"), any(NotificationDto.class));
    }

    @Test
    void 메시지_파싱_실패_시_예외를_삼키고_종료() throws Exception {
        // given
        given(objectMapper.readValue(anyString(), eq(DomainEvent.class)))
            .willThrow(new RuntimeException("JSON parse error"));

        // when & then
        assertThatCode(() -> consumer.consume("{invalid-json}"))
            .doesNotThrowAnyException();
    }
}