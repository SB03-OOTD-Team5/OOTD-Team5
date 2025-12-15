package com.sprint.ootd5team.domain.directmessage.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.sprint.ootd5team.domain.directmessage.service.DirectMessageWsService;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DirectMessageWsController 테스트")
@TestClassOrder(ClassOrderer.DisplayName.class)
class DirectMessageWsControllerTest {

    @Mock
    private DirectMessageWsService wsService;

    @InjectMocks
    private DirectMessageWsController controller;

    @Test
    @DisplayName("1. 성공: Send 호출 시 WS 서비스로 payload 전달")
    void send_success() throws Exception {
        String payload = "{\"message\":\"hello\"}";

        controller.send(payload);

        verify(wsService).handleSend(payload);
    }

    @Test
    @DisplayName("2. 실패: WS 서비스가 예외를 던지면 그대로 전파")
    void send_propagatesException() throws Exception {
        String payload = "invalid";
        doThrow(new IllegalStateException("error")).when(wsService).handleSend(payload);

        assertThatThrownBy(() -> controller.send(payload))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("error");
    }
}
