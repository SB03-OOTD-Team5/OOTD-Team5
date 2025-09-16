package com.sprint.ootd5team.feed.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.config.SecurityConfig;
import com.sprint.ootd5team.base.security.JwtAuthenticationFilter;
import com.sprint.ootd5team.base.security.OotdUserDetails;
import com.sprint.ootd5team.domain.feed.controller.FeedController;
import com.sprint.ootd5team.domain.feed.dto.request.FeedListRequest;
import com.sprint.ootd5team.domain.feed.dto.response.FeedDtoCursorResponse;
import com.sprint.ootd5team.domain.feed.service.FeedService;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import com.sprint.ootd5team.domain.user.entity.Role;
import java.time.Instant;
import java.util.Collections;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = FeedController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
    },
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
    })
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("FeedController 슬라이스 테스트")
@ActiveProfiles("test")
public class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeedService feedService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("피드 목록 조회 성공")
    void getFeeds_success() throws Exception {
        // given
        UUID testUserId = UUID.randomUUID();
        OotdUserDetails ootdUser = createTestUser(testUserId, Role.USER);
        Authentication auth = new UsernamePasswordAuthenticationToken(ootdUser, null, ootdUser.getAuthorities());

        FeedDtoCursorResponse response = new FeedDtoCursorResponse(
            Collections.emptyList(),
            "next-cursor",
            UUID.randomUUID(),
            true,
            100L,
            "createdAt",
            "ASCENDING"
        );

        when(feedService.getFeeds(any(FeedListRequest.class), eq(testUserId)))
            .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/feeds")
                .param("cursor", "0")
                .param("limit", "10")
                .param("sortBy", "createdAt")
                .param("sortDirection", "ASCENDING")
                .principal(auth)
            )
            .andExpect(status().isOk());
    }

    private OotdUserDetails createTestUser(UUID userId, Role role) {
        UserDto userDto = new UserDto(
            userId,
            Instant.now(),
            "test@example.com",
            "tester",
            role,
            List.of(),
            false
        );
        return new OotdUserDetails(userDto, "password");
    }
}