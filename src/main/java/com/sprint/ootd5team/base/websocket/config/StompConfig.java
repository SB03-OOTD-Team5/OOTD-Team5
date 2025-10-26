package com.sprint.ootd5team.base.websocket.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private static final String ENDPOINT_PATH = "/ws";

    private final List<String> allowedOrigins;
    private final Environment environment;
    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    static final String ATTR_HTTP_AUTHORIZATION = "HTTP_AUTHORIZATION";
    static final String ATTR_QUERY_ACCESS_TOKEN = "QUERY_ACCESS_TOKEN";

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(ENDPOINT_PATH)
            .setAllowedOrigins(allowedOrigins.toArray(new String[0]))
            .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/pub");
        registry.setUserDestinationPrefix("/user");

        if (environment.acceptsProfiles(Profiles.of("prod"))) {
            configureExternalRelay(registry);
        } else {
            log.info("[STOMP] 메모리 기반 SimpleBroker 사용 (profiles={})", (Object) environment.getActiveProfiles());
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
        registration.interceptors(stompAuthChannelInterceptor, loggingInterceptor(Direction.INBOUND));
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(loggingInterceptor(Direction.OUTBOUND));
        registration.taskExecutor(new ThreadPoolTaskExecutorBuilder()
            .corePoolSize(4)
            .maxPoolSize(16)
            .queueCapacity(500)
            .threadNamePrefix("stomp-out-")
            .build());
    }

    private ChannelInterceptor loggingInterceptor(Direction direction) {
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                SimpMessageType type = (SimpMessageType) message.getHeaders().get("simpMessageType");
                if (type == SimpMessageType.HEARTBEAT || type == SimpMessageType.CONNECT_ACK) {
                    return message;
                }

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
