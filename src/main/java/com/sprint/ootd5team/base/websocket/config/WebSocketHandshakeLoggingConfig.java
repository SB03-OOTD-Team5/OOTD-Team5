package com.sprint.ootd5team.base.websocket.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Slf4j
@Configuration
@EnableWebSocket // STOMP 외의 저수준 웹소켓 설정이 필요할 때 사용(여기선 인터셉터 예시)
public class WebSocketHandshakeLoggingConfig implements WebSocketConfigurer {
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		// 아래는 엔드포인트를 또 여는 게 아니라, 인터셉터 예시를 보여주기 위함
		// 실제 서비스에선 STOMP 엔드포인트에 핸드셰이크 인터셉터를 추가해도 됨
		registry.addHandler(new org.springframework.web.socket.handler.TextWebSocketHandler() {}, "/_internal-log")
			.addInterceptors(new HttpSessionHandshakeInterceptor() {
				public boolean beforeHandshake(
					HttpServletRequest request,
					HttpServletResponse response,
					org.springframework.web.socket.WebSocketHandler wsHandler,
					java.util.Map<String, Object> attributes) throws Exception {
					// 연결 시점의 요청 정보 기록(헤더/쿼리스트링/세션 등)
					log.debug("[HANDSHAKE] uri={}, headers={}", request.getRequestURI(), request.getHeaderNames());
					return super.beforeHandshake((ServerHttpRequest) request,
						(ServerHttpResponse) response, wsHandler, attributes);
				}
			})
			.setAllowedOrigins("*");
	}
}