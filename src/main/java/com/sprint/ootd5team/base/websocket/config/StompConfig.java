package com.sprint.ootd5team.base.websocket.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.StompBrokerRelayRegistration;
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
    private final List<String> allowedOrigins;
    private final Environment environment;
    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    // 세션에 저장해둘 키
    static final String ATTR_HTTP_AUTHORIZATION = "HTTP_AUTHORIZATION";
    static final String ATTR_QUERY_ACCESS_TOKEN = "QUERY_ACCESS_TOKEN";

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트
        registry.addEndpoint(ENDPOINT_PATH)
            .setAllowedOrigins(allowedOrigins.toArray(new String[0])) //CORS설정
            .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 발행(publish) prefix → @MessageMapping으로 라우팅
        registry.setApplicationDestinationPrefixes("/pub");
        registry.setUserDestinationPrefix("/user");

        if (environment.acceptsProfiles(Profiles.of("prod"))) {
            configureExternalRelay(registry);
        } else {
            log.info("[STOMP] 메모리 기반 SimpleBroker 사용 (profiles={})", (Object) environment.getActiveProfiles());
            // 구독(subscribe) prefix → 메시지 브로커로 전달
            registry.enableSimpleBroker("/sub");
        }
    }

    private void configureExternalRelay(MessageBrokerRegistry registry) {
        String host = environment.getProperty("app.messaging.relay.host", "localhost");
        int port = environment.getProperty("app.messaging.relay.port", Integer.class, 61613);
        String clientLogin = environment.getProperty("app.messaging.relay.login", "guest");
        String clientPasscode = environment.getProperty("app.messaging.relay.passcode", "guest");
        String systemLogin = environment.getProperty("app.messaging.relay.system-login", clientLogin);
        String systemPasscode = environment.getProperty("app.messaging.relay.system-passcode", clientPasscode);
        String virtualHost = environment.getProperty("app.messaging.relay.virtual-host", "/");
        int heartbeatSend = environment.getProperty(
            "app.messaging.relay.heartbeat-send-interval", Integer.class, 10000);
        int heartbeatReceive = environment.getProperty(
            "app.messaging.relay.heartbeat-receive-interval", Integer.class, 10000);

        log.info("[STOMP] RabbitMQ 연결 host={} port={} vhost={}", host, port, virtualHost);

        StompBrokerRelayRegistration relay = registry.enableStompBrokerRelay("/sub");
        relay.setRelayHost(host);
        relay.setRelayPort(port);
        relay.setClientLogin(clientLogin);
        relay.setClientPasscode(clientPasscode);
        relay.setSystemLogin(systemLogin);
        relay.setSystemPasscode(systemPasscode);
        relay.setVirtualHost(virtualHost);
        relay.setSystemHeartbeatSendInterval(heartbeatSend);
        relay.setSystemHeartbeatReceiveInterval(heartbeatReceive);
        relay.setAutoStartup(true);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.taskExecutor(new ThreadPoolTaskExecutorBuilder()
            .corePoolSize(4)
            .maxPoolSize(16)
            .queueCapacity(500)
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
            .queueCapacity(500)
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
                    return message;
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
