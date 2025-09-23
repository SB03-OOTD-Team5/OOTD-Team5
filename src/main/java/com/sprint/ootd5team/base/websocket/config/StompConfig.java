package com.sprint.ootd5team.base.websocket.config;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker // STOMP 브로커 활성화
@RequiredArgsConstructor
public class StompConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// Websocket 연결 엔드포인트
		registry.addEndpoint("/ws")    // ws://localhost:8080/ws
			.setAllowedOrigins("http://localhost:8080")             // CORS 내 로컬에 대해 개방
			.addInterceptors(new HttpHandshakeAuthInterceptor())
			.withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		// 발행(publish) prefix → @MessageMapping으로 라우팅
		config.setApplicationDestinationPrefixes("/pub");
		// 구독(subscribe) prefix → 메시지 브로커로 전달
		config.enableSimpleBroker("/sub");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		// 클라이언트 → 서버 방향(INBOUND) 메시지를 가로채 로그
		registration.interceptors(loggingInterceptor("INBOUND"));
	}

	@Override
	public void configureClientOutboundChannel(ChannelRegistration registration) {
		// 서버 → 클라이언트 방향(OUTBOUND) 메시지를 가로채 로그
		registration.interceptors(loggingInterceptor("OUTBOUND"));
	}

	// 공통 로깅 인터셉터 - 헤더와 페이로드
	private ChannelInterceptor loggingInterceptor(String direction) {
		return new ChannelInterceptor() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				log.debug("[{}] headers={}, payload={}", direction, message.getHeaders(), message.getPayload());
				return message; // 메시지를 그대로 통과
			}
		};
	}
	/**
	 * 핸드셰이크 전 HTTP Authorization 헤더,
	 * access_token을 세션 속성에 저장.
	 */
	private static class HttpHandshakeAuthInterceptor implements HandshakeInterceptor {
		@Override
		public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
			WebSocketHandler wsHandler, Map<String, Object> attributes) {
			if (request instanceof ServletServerHttpRequest servletReq) {
				String authorization = servletReq.getServletRequest().getHeader("Authorization");
				if (StringUtils.hasText(authorization)) {
					attributes.put("HTTP_AUTHORIZATION", authorization);
				}
				// SockJS XHR/EventSource에서 쿼리로 전달한 경우 지원 (예: ?access_token=...)
				String tokenFromQuery = servletReq.getServletRequest().getParameter("access_token");
				if (StringUtils.hasText(tokenFromQuery)) {
					attributes.put("QUERY_ACCESS_TOKEN", tokenFromQuery);
				}
			}
			return true;
		}

		@Override
		public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
			WebSocketHandler wsHandler, Exception exception) {
		}
	}

}
