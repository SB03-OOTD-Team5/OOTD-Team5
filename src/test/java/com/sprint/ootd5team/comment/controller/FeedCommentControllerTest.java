package com.sprint.ootd5team.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.config.SecurityConfig;
import com.sprint.ootd5team.base.security.JwtAuthenticationFilter;
import com.sprint.ootd5team.domain.comment.controller.FeedCommentController;
import com.sprint.ootd5team.domain.comment.dto.request.CommentListRequest;
import com.sprint.ootd5team.domain.comment.dto.response.CommentDtoCursorResponse;
import com.sprint.ootd5team.domain.comment.service.FeedCommentService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = FeedCommentController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
    },
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
    })
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("FeedCommentController 슬라이스 테스트")
@ActiveProfiles("test")
public class FeedCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeedCommentService feedCommentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("피드 댓글 조회 성공")
    void getComments_success() throws Exception {
        // given
        UUID feedId = UUID.randomUUID();

        CommentDtoCursorResponse response = new CommentDtoCursorResponse(
            List.of(),
            "2025-09-20T10:00:00Z",
            UUID.randomUUID(),
            false,
            5L,
            "createdAt",
            "DESC"
        );

        given(feedCommentService.getComments(eq(feedId), any(CommentListRequest.class)))
            .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/feeds/{feedId}/comments", feedId.toString())
                .param("cursor", "2025-09-20T09:00:00Z")
                .param("idAfter", UUID.randomUUID().toString())
                .param("limit", String.valueOf(10))
                .accept(MediaType.APPLICATION_JSON)
             )
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }
}