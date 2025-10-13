package com.sprint.ootd5team.base.exception;

import com.sprint.ootd5team.base.sse.controller.SseController;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * SSE 전용 예외 처리기
 * 일반 api 전역 예외처리는 json(ErrorResponse)를 반환
 * sse 엔드포인트의 경우 content-type이 text/event-stream으로 열려있어 json직렬화 응답을 보낼 수 없음
 * sse Controller 범위에서 발생하는 예외를 가로채 sse 프로토콜 형식 (event/data 프레임)으로 클라이언트에 전송
 */
@Slf4j
@ControllerAdvice(assignableTypes = SseController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SseExceptionHandler {

    /**
     * SSE 전용 예외 핸들러
     *
     * @param ex       SSE 처리 중 발생한 예외
     * @param response 클라이언트 응답 스트림
     */
    @ExceptionHandler(Exception.class)
    public void handleSseException(Exception ex, HttpServletResponse response) {
        try {
            log.error("[SseExceptionHandler] SSE 요청 중 예외 발생: type={}, message={}",
                ex.getClass().getSimpleName(), ex.getMessage(), ex);

            if (response.isCommitted()) {
                log.warn("[SseExceptionHandler] 응답이 이미 커밋되어 SSE error 이벤트 전송 불가");
                return;
            }

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/event-stream");
            response.getWriter().write("event: error\n");
            response.getWriter().write("data: 서버 오류가 발생했습니다.\n\n");
            response.flushBuffer();

            log.info("[SseExceptionHandler] SSE error 이벤트 전송 완료");
        } catch (IllegalStateException ise) {
            log.warn("[SseExceptionHandler] 응답 스트림이 이미 닫혔거나 커밋됨: {}", ise.getMessage());
        } catch (IOException io) {
            log.warn("[SseExceptionHandler] SSE error 이벤트 전송 실패: {}", io.getMessage());
        }
    }
}