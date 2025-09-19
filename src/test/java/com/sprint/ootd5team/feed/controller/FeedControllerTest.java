package com.sprint.ootd5team.feed.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.config.SecurityConfig;
import com.sprint.ootd5team.base.security.JwtAuthenticationFilter;
import com.sprint.ootd5team.base.security.OotdUserDetails;
import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.domain.feed.controller.FeedController;
import com.sprint.ootd5team.domain.feed.dto.data.FeedDto;
import com.sprint.ootd5team.domain.feed.dto.request.FeedCreateRequest;
import com.sprint.ootd5team.domain.feed.dto.request.FeedListRequest;
import com.sprint.ootd5team.domain.feed.dto.request.FeedUpdateRequest;
import com.sprint.ootd5team.domain.feed.dto.response.FeedDtoCursorResponse;
import com.sprint.ootd5team.domain.feed.service.FeedService;
import com.sprint.ootd5team.domain.user.dto.AuthorDto;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.weather.dto.data.PrecipitationDto;
import com.sprint.ootd5team.domain.weather.dto.data.TemperatureDto;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherSummaryDto;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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

    @MockitoBean
    private AuthService authService;

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

    @Test
    @DisplayName("피드 삭제 성공")
    void deleteFeed_success() throws Exception {
        // given
        UUID feedId = UUID.randomUUID();

        doNothing().when(feedService).delete(feedId);

        // when & then
        mockMvc.perform(delete("/api/feeds/{feedId}", feedId)
                .with(csrf()))
            .andExpect(status().isNoContent());

        verify(feedService, times(1)).delete(feedId);
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

    @Test
    @DisplayName("피드 수정 성공")
    void updateFeed_success() throws Exception {
        // given
        UUID feedId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        FeedUpdateRequest request = new FeedUpdateRequest("수정된 내용");
        AuthorDto author = new AuthorDto(userId, "tester", "https://example.com/profile.png");
        WeatherSummaryDto weather = new WeatherSummaryDto(
            UUID.randomUUID(),
            SkyStatus.CLOUDY,
            new PrecipitationDto(PrecipitationType.RAIN, 0.0, 0.0),
            new TemperatureDto(20.0, 0.0, 15.0, 25.0)
        );

        FeedDto responseDto = new FeedDto(
            feedId,
            Instant.now(),
            Instant.now(),
            author,
            weather,
            List.of(),
            "수정된 내용",
            5,
            3,
            true
        );

        // mocking
        given(authService.getCurrentUserId()).willReturn(userId);
        given(feedService.update(eq(feedId), any(FeedUpdateRequest.class), eq(userId)))
            .willReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/api/feeds/{feedId}", feedId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(feedId.toString()))
            .andExpect(jsonPath("$.content").value("수정된 내용"))
            .andExpect(jsonPath("$.likeCount").value(5))
            .andExpect(jsonPath("$.commentCount").value(3));

        then(authService).should().getCurrentUserId();
        then(feedService).should().update(eq(feedId), any(FeedUpdateRequest.class), eq(userId));
    }

    @Test
    @DisplayName("피드 등록 성공")
    void createFeed_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID feedId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();
        UUID clothesId1 = UUID.randomUUID();
        UUID clothesId2 = UUID.randomUUID();

        FeedCreateRequest request = new FeedCreateRequest(
            userId,
            weatherId,
            Set.of(clothesId1, clothesId2),
            "오늘의 피드 내용"
        );

        FeedDto responseDto = new FeedDto(
            feedId,
            Instant.now(),
            Instant.now(),
            new AuthorDto(userId, "tester", "profile.png"),
            new WeatherSummaryDto(weatherId, SkyStatus.CLEAR, null, null),
            List.of(),
            "오늘의 피드 내용",
            0L,
            0L,
            false
        );

        given(authService.getCurrentUserId()).willReturn(userId);
        given(feedService.create(any(FeedCreateRequest.class), eq(userId))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/feeds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(feedId.toString()))
            .andExpect(jsonPath("$.content").value("오늘의 피드 내용"))
            .andExpect(jsonPath("$.author.userId").value(userId.toString()))
            .andDo(print());

        then(feedService).should().create(any(FeedCreateRequest.class), eq(userId));
        then(authService).should().getCurrentUserId();
    }
}