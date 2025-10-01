package com.sprint.ootd5team.base.websocket.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class StompConfig implements WebSocketMessageBrokerConfigurer {

    // === 엔드포인트 경로 ===
    private static final String ENDPOINT_PATH = "/ws";
    // === CORS 허용 ORIGIN 목록 ===
    private static final String[] ALLOWED_ORIGINS = {"http://localhost:8080"};
    // 세션에 저장해둘 키
    static final String ATTR_HTTP_AUTHORIZATION = "HTTP_AUTHORIZATION";
    static final String ATTR_QUERY_ACCESS_TOKEN = "QUERY_ACCESS_TOKEN";


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트
        registry.addEndpoint(ENDPOINT_PATH)
            .setAllowedOrigins(ALLOWED_ORIGINS) //CORS설정
            .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 발행(publish) prefix → @MessageMapping으로 라우팅
        registry.setApplicationDestinationPrefixes("/pub");
        // 구독(subscribe) prefix → 메시지 브로커로 전달
        registry.enableSimpleBroker("/sub");
    }

    // 인증/권한 채널 인터셉터 주입
    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.taskExecutor(new ThreadPoolTaskExecutorBuilder()
            .corePoolSize(4)
            .maxPoolSize(16)
            .queueCapacity(200)
            .threadNamePrefix("stomp-in-")
            .build());
        // 클라이언트 → 서버 방향(INBOUND) 메시지를 가로채 로그
        registration.interceptors(stompAuthChannelInterceptor, loggingInterceptor(Direction.INBOUND));
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // 서버 → 클라이언트 방향(OUTBOUND) 메시지를 가로채 로그
        registration.interceptors(loggingInterceptor(Direction.OUTBOUND));
        registration.taskExecutor(new ThreadPoolTaskExecutorBuilder()
            .corePoolSize(4)
            .maxPoolSize(16)
            .queueCapacity(200)
            .threadNamePrefix("stomp-out-")
            .build());
    }

    // ====== 공통 로깅 인터셉터 ======
    private ChannelInterceptor loggingInterceptor(Direction direction) {
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                // 너무 상세한 로그 방지: HEARTBEAT/CONNECT_ACK 등은 스킵
                SimpMessageType type = (SimpMessageType) message.getHeaders().get("simpMessageType");
                if (type == SimpMessageType.HEARTBEAT || type == SimpMessageType.CONNECT_ACK) {
                    return message; // 무시
                }

                // payload가 byte[]이면 길이만 출력 (민감정보 노출 방지)
                Object payload = message.getPayload();
                int payloadSize = (payload instanceof byte[])
                    ? ((byte[]) payload).length
                    : (payload != null ? String.valueOf(payload).length() : 0);

                log.debug("[{}] type={}, headers={}, payloadSize={}",
                    direction, type, message.getHeaders(), payloadSize);
                return message;
            }
        };
    }

    private enum Direction {
        INBOUND, OUTBOUND
    }
}
