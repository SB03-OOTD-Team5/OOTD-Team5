package com.sprint.ootd5team.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.domain.notification.controller.NotificationController;
import com.sprint.ootd5team.domain.notification.dto.response.NotificationDto;
import com.sprint.ootd5team.domain.notification.dto.response.NotificationDtoCursorResponse;
import com.sprint.ootd5team.domain.notification.enums.NotificationLevel;
import com.sprint.ootd5team.domain.notification.service.NotificationService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
@DisplayName("NotificationController 슬라이스 테스트")
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("알림 전체 조회 성공")
    void getNotifications_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        given(authService.getCurrentUserId()).willReturn(userId);

        NotificationDtoCursorResponse mockResponse = new NotificationDtoCursorResponse(
            List.of(
                new NotificationDto(UUID.randomUUID(), Instant.now(), userId, "알림제목", "알림 내용",
                    NotificationLevel.INFO)
            ),
            null,
            null,
            true,
            null,
            "createdAt",
            "DESC"
        );

        given(
            notificationService.findAll(eq(userId), any(), any(), eq(10), eq(Sort.Direction.DESC)))
            .willReturn(mockResponse);

        // when & then
        mockMvc.perform(
                get("/api/notifications")
                    .param("limit", "10")
                    .param("direction", "DESC")
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].title").value("알림제목"))
            .andExpect(jsonPath("$.data[0].content").value("알림 내용"))
            .andExpect(jsonPath("$.data[0].level").value("INFO"))
            .andExpect(jsonPath("$.hasNext").value(true));
    }
}
