package com.sprint.ootd5team.follow.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.config.SecurityConfig;
import com.sprint.ootd5team.base.security.JwtAuthenticationFilter;
import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.domain.feed.dto.enums.SortDirection;
import com.sprint.ootd5team.domain.follow.controller.FollowController;
import com.sprint.ootd5team.domain.follow.dto.data.FollowDto;
import com.sprint.ootd5team.domain.follow.dto.data.FollowSummaryDto;
import com.sprint.ootd5team.domain.follow.dto.request.FollowCreateRequest;
import com.sprint.ootd5team.domain.follow.dto.request.FollowerListRequest;
import com.sprint.ootd5team.domain.follow.dto.request.FollowingListRequest;
import com.sprint.ootd5team.domain.follow.dto.response.FollowListResponse;
import com.sprint.ootd5team.domain.follow.service.FollowService;
import com.sprint.ootd5team.domain.user.dto.AuthorDto;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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

@WebMvcTest(controllers = FollowController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
    },
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
    })
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("FollowController 슬라이스 테스트")
@ActiveProfiles("test")
public class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FollowService followService;

    @MockitoBean
    private AuthService authService;

    private UUID followerId;
    private FollowListResponse listResponse;

    @BeforeEach
    void setUp() {
        followerId = UUID.randomUUID();
        AuthorDto user1 = new AuthorDto(UUID.randomUUID(), "user1", "https://example.com/profile1.png");
        AuthorDto user2 = new AuthorDto(UUID.randomUUID(), "user2", "https://example.com/profile2.png");
        AuthorDto user3 = new AuthorDto(UUID.randomUUID(), "user3", "https://example.com/profile3.png");

        List<FollowDto> data = List.of(
            new FollowDto(UUID.randomUUID(), user2, user1),
            new FollowDto(UUID.randomUUID(), user3, user1),
            new FollowDto(UUID.randomUUID(), user2, user3)
        );
        listResponse = new FollowListResponse(
            data, null, null, false, 3, "createdAt", SortDirection.DESCENDING
        );
    }

    @Test
    @DisplayName("팔로잉 목록 조회 성공")
    void getFollowings_success() throws Exception {
        // given
        given(followService.getFollowingList(any(FollowingListRequest.class)))
            .willReturn(listResponse);

        // when & then
        mockMvc.perform(get("/api/follows/followings")
                .param("followerId", followerId.toString())
                .param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].followee.name").value("user2"))
            .andExpect(jsonPath("$.data[1].followee.name").value("user3"));
    }

    @Test
    @DisplayName("팔로잉 목록 조회 중 잘못된 요청 파라미터로 검증 실패 시 400 반환")
    void getFollowings_validationFail() throws Exception {
        mockMvc.perform(get("/api/follows/followings")
                .param("followerId", "")
                .param("limit", "-1"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("팔로워 목록 조회 성공")
    void getFollowers_success() throws Exception {
        // given
        given(followService.getFollowerList(any(FollowerListRequest.class)))
            .willReturn(listResponse);

        // when & then
        mockMvc.perform(get("/api/follows/followers")
                .param("followeeId", followerId.toString())
                .param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].follower.name").value("user1"))
            .andExpect(jsonPath("$.data[2].follower.name").value("user3"));
    }

    @Test
    @DisplayName("팔로워 목록 조회 중 잘못된 요청 파라미터로 검증 실패 시 400 반환")
    void getFollowers_validationFail() throws Exception {
        mockMvc.perform(get("/api/follows/followers")
                .param("followeeId", "")
                .param("limit", "-1"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("팔로우 요약 조회 성공")
    void getSummary_shouldReturnSummaryDto() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        FollowSummaryDto summaryDto = new FollowSummaryDto(
            userId, 10L, 5L, true, currentUserId, false
        );

        given(authService.getCurrentUserId()).willReturn(currentUserId);
        given(followService.getSummary(eq(userId), eq(currentUserId)))
            .willReturn(summaryDto);

        // when & then
        mockMvc.perform(get("/api/follows/summary")
                .param("userId", userId.toString())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.followeeId").value(userId.toString()))
            .andExpect(jsonPath("$.followerCount").value(10))
            .andExpect(jsonPath("$.followingCount").value(5))
            .andExpect(jsonPath("$.followedByMe").value(true))
            .andExpect(jsonPath("$.followedByMeId").value(currentUserId.toString()))
            .andExpect(jsonPath("$.followingMe").value(false));

        then(authService).should().getCurrentUserId();
        then(followService).should().getSummary(eq(userId), eq(currentUserId));
    }

    @Test
    @DisplayName("팔로우 등록 성공")
    void createFollow_shouldReturnCreatedFollow() throws Exception {
        // given
        UUID followeeId = UUID.randomUUID();
        UUID followId = UUID.randomUUID();

        AuthorDto follower = new AuthorDto(followerId, "follower", "https://example.com/follower.png");
        AuthorDto followee = new AuthorDto(followeeId, "followee", "https://example.com/followee.png");
        FollowDto followDto = new FollowDto(followId, followee, follower);

        given(followService.follow(any(UUID.class), any(UUID.class)))
            .willReturn(followDto);

        FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);
        String requestJson = new ObjectMapper().writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/follows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(followId.toString()))
            .andExpect(jsonPath("$.follower.name").value("follower"))
            .andExpect(jsonPath("$.followee.name").value("followee"));
    }

    @Test
    @DisplayName("팔로우 취소 성공")
    void unFollow_shouldReturnNoContent() throws Exception {
        // given
        UUID followId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        given(authService.getCurrentUserId()).willReturn(currentUserId);

        // when & then
        mockMvc.perform(delete("/api/follows/{followId}", followId))
            .andExpect(status().isNoContent());

        verify(followService).unFollow(followId, currentUserId);
    }
}