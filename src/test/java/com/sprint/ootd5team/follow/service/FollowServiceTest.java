package com.sprint.ootd5team.follow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.domain.feed.dto.enums.SortDirection;
import com.sprint.ootd5team.domain.follow.dto.data.FollowDto;
import com.sprint.ootd5team.domain.follow.dto.data.FollowProjectionDto;
import com.sprint.ootd5team.domain.follow.dto.request.FollowListRequest;
import com.sprint.ootd5team.domain.follow.dto.response.FollowListResponse;
import com.sprint.ootd5team.domain.follow.mapper.FollowMapper;
import com.sprint.ootd5team.domain.follow.repository.FollowRepository;
import com.sprint.ootd5team.domain.follow.service.FollowServiceImpl;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.user.dto.AuthorDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowService 슬라이스 테스트")
@ActiveProfiles("test")
public class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private FollowMapper followMapper;

    @InjectMocks
    private FollowServiceImpl followService;

    private UUID followerId;
    private FollowProjectionDto projection1;
    private FollowProjectionDto projection2;
    private AuthorDto user1;
    private AuthorDto user2;
    private AuthorDto user3;


    @BeforeEach
    void setUp() {
        followerId = UUID.randomUUID();

        user1 = new AuthorDto(UUID.randomUUID(), "user1", "https://example.com/profile1.png");
        user2 = new AuthorDto(UUID.randomUUID(), "user2", "https://example.com/profile2.png");
        user3 = new AuthorDto(UUID.randomUUID(), "user3", "https://example.com/profile3.png");

        projection1 = new FollowProjectionDto(
            UUID.randomUUID(),
            Instant.now(),
            user2,
            user1
            );

        projection2 = new FollowProjectionDto(
            UUID.randomUUID(),
            Instant.now().plusSeconds(5),
            user3,
            user1
        );
    }

    @Test
    @DisplayName("팔로잉 목록 조회 성공 - hasNext=false")
    void getFollowingList_success_noHasNext() {
        // given
        FollowListRequest request = new FollowListRequest(followerId, null, null, 10, null);

        given(profileRepository.existsByUserId(followerId)).willReturn(true);
        given(followRepository.findByFollowIdWithCursor(eq(followerId), any(), any(), eq(10), any()))
            .willReturn(List.of(projection1, projection2));
        given(followMapper.toFollowDtoList(anyList()))
            .willReturn(List.of(
                new FollowDto(projection1.id(), user3, user1),
                new FollowDto(projection2.id(), user2, user1)
            ));
        given(followRepository.countByFollowerId(followerId)).willReturn(2L);

        // when
        FollowListResponse response = followService.getFollowingList(request);

        // then
        assertThat(response.data()).hasSize(2);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.totalCount()).isEqualTo(2);
        assertThat(response.sortBy()).isEqualTo("createdAt");
        assertThat(response.sortDirection()).isEqualTo(SortDirection.DESCENDING);
    }

    @Test
    @DisplayName("팔로잉 목록 조회 성공 - hasNext=true (limit보다 많은 결과)")
    void getFollowingList_success_hasNext() {
        // given
        FollowListRequest request = new FollowListRequest(followerId, null, null, 1, null);

        given(profileRepository.existsByUserId(followerId)).willReturn(true);
        given(followRepository.findByFollowIdWithCursor(eq(followerId), any(), any(), eq(1), any()))
            .willReturn(List.of(projection1, projection2));
        given(followMapper.toFollowDtoList(argThat(list -> list.size() == 1)))
            .willReturn(List.of(new FollowDto(projection1.id(), user3, user1)));
        given(followRepository.countByFollowerId(followerId)).willReturn(2L);

        // when
        FollowListResponse response = followService.getFollowingList(request);

        // then
        assertThat(response.data()).hasSize(1);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isNotNull();
        assertThat(response.nextIdAfter()).isNotNull();
    }

    @Test
    @DisplayName("프로필이 존재하지 않으면 ProfileNotFoundException 발생")
    void getFollowingList_profileNotFound() {
        // given
        FollowListRequest request = new FollowListRequest(followerId, null, null, 10, null);

        given(profileRepository.existsByUserId(followerId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> followService.getFollowingList(request))
            .isInstanceOf(ProfileNotFoundException.class);
    }
}