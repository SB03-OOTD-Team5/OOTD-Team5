package com.sprint.ootd5team.domain.follow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sprint.ootd5team.base.exception.follow.FollowAlreadyDeletedException;
import com.sprint.ootd5team.base.exception.follow.FollowNotFoundException;
import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.domain.feed.dto.enums.SortDirection;
import com.sprint.ootd5team.domain.follow.dto.data.FollowDto;
import com.sprint.ootd5team.domain.follow.dto.data.FollowProjectionDto;
import com.sprint.ootd5team.domain.follow.dto.data.FollowSummaryDto;
import com.sprint.ootd5team.domain.follow.dto.enums.FollowDirection;
import com.sprint.ootd5team.domain.follow.dto.request.FollowerListRequest;
import com.sprint.ootd5team.domain.follow.dto.request.FollowingListRequest;
import com.sprint.ootd5team.domain.follow.dto.response.FollowListResponse;
import com.sprint.ootd5team.domain.follow.entity.Follow;
import com.sprint.ootd5team.domain.follow.mapper.FollowMapper;
import com.sprint.ootd5team.domain.follow.repository.FollowRepository;
import com.sprint.ootd5team.domain.notification.event.type.single.FollowCreatedEvent;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.mapper.ProfileMapper;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.user.dto.AuthorDto;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Mock
    private ProfileMapper profileMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FollowServiceImpl followService;

    private UUID currentUserId = UUID.randomUUID();
    private UUID followId;
    private UUID followerId;
    private UUID followeeId;
    private FollowProjectionDto projection1;
    private FollowProjectionDto projection2;
    private AuthorDto user1;
    private AuthorDto user2;
    private AuthorDto user3;


    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        followId = UUID.randomUUID();
        followerId = UUID.randomUUID();
        followeeId = UUID.randomUUID();

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
        FollowingListRequest request = new FollowingListRequest(
            followerId, null, null, 10, null
        );

        given(profileRepository.existsByUserId(followerId)).willReturn(true);
        given(followRepository.findByCursor(eq(followerId), any(), any(), eq(10), any(), eq(FollowDirection.FOLLOWING)))
            .willReturn(List.of(projection1, projection2));
        given(followMapper.toFollowDtoList(anyList()))
            .willReturn(List.of(
                new FollowDto(projection1.id(), user3, user1),
                new FollowDto(projection2.id(), user2, user1)
            ));
        given(followRepository.countByUserIdAndNameLike(followerId, request.nameLike(), FollowDirection.FOLLOWING))
            .willReturn(2L);

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
    @DisplayName("팔로잉 목록 조회 성공 - hasNext=true")
    void getFollowingList_success_hasNext() {
        // given
        FollowingListRequest request = new FollowingListRequest(
            followerId, null, null, 1, null
        );

        given(profileRepository.existsByUserId(followerId)).willReturn(true);
        given(followRepository.findByCursor(eq(followerId), any(), any(), eq(1), any(), eq(FollowDirection.FOLLOWING)))
            .willReturn(List.of(projection1, projection2));
        given(followMapper.toFollowDtoList(argThat(list -> list.size() == 1)))
            .willReturn(List.of(new FollowDto(projection1.id(), user3, user1)));
        given(followRepository.countByUserIdAndNameLike(followerId, request.nameLike(), FollowDirection.FOLLOWING))
            .willReturn(2L);

        // when
        FollowListResponse response = followService.getFollowingList(request);

        // then
        assertThat(response.data()).hasSize(1);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isNotNull();
        assertThat(response.nextIdAfter()).isNotNull();
    }

    @Test
    @DisplayName("팔로워 목록 조회 성공 - hasNext=false")
    void getFollowerList_success_noHasNext() {
        // given
        FollowerListRequest request = new FollowerListRequest(
            followerId, null, null, 10, null
        );

        given(profileRepository.existsByUserId(followerId)).willReturn(true);
        given(followRepository.findByCursor(eq(followerId), any(), any(), eq(10), any(), eq(FollowDirection.FOLLOWER)))
            .willReturn(List.of(projection1, projection2));
        given(followMapper.toFollowDtoList(anyList()))
            .willReturn(List.of(
                new FollowDto(projection1.id(), user2, user1),
                new FollowDto(projection2.id(), user3, user1)
            ));
        given(followRepository.countByUserIdAndNameLike(followerId, request.nameLike(), FollowDirection.FOLLOWER))
            .willReturn(2L);

        // when
        FollowListResponse response = followService.getFollowerList(request);

        // then
        assertThat(response.data()).hasSize(2);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.totalCount()).isEqualTo(2);
        assertThat(response.sortBy()).isEqualTo("createdAt");
        assertThat(response.sortDirection()).isEqualTo(SortDirection.DESCENDING);
    }

    @Test
    @DisplayName("팔로워 목록 조회 성공 - hasNext=true")
    void getFollowerList_success_hasNext() {
        // given
        FollowerListRequest request = new FollowerListRequest(
            followerId, null, null, 1, null
        );

        given(profileRepository.existsByUserId(followerId)).willReturn(true);
        given(followRepository.findByCursor(eq(followerId), any(), any(), eq(1), any(), eq(FollowDirection.FOLLOWER)))
            .willReturn(List.of(projection1, projection2));
        given(followMapper.toFollowDtoList(argThat(list -> list.size() == 1)))
            .willReturn(List.of(new FollowDto(projection1.id(), user2, user1)));
        given(followRepository.countByUserIdAndNameLike(followerId, request.nameLike(), FollowDirection.FOLLOWER))
            .willReturn(2L);

        // when
        FollowListResponse response = followService.getFollowerList(request);

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
        FollowingListRequest request = new FollowingListRequest(
            followerId, null, null, 10, null
        );

        given(profileRepository.existsByUserId(followerId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> followService.getFollowingList(request))
            .isInstanceOf(ProfileNotFoundException.class);
    }

    @Test
    @DisplayName("팔로우 요약 정보 조회 성공")
    void getSummary_shouldReturnDto() {
        // given
        UUID userId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        FollowSummaryDto expected = new FollowSummaryDto(
            userId, 10L, 5L, true, currentUserId, false
        );

        given(profileRepository.existsByUserId(any(UUID.class)))
            .willReturn(true);

        given(followRepository.getSummary(userId, currentUserId))
            .willReturn(expected);

        // when
        FollowSummaryDto actual = followService.getSummary(userId, currentUserId);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.followeeId()).isEqualTo(userId);
        assertThat(actual.followerCount()).isEqualTo(10L);
        assertThat(actual.followingCount()).isEqualTo(5L);
        assertThat(actual.followedByMe()).isTrue();
        assertThat(actual.followedByMeId()).isEqualTo(currentUserId);
        assertThat(actual.followingMe()).isFalse();

        then(followRepository).should().getSummary(userId, currentUserId);
    }

    @Test
    @DisplayName("팔로우 등록 성공(알림 이벤트 발행 포함)")
    void createFollow_success() {
        // given
        Profile followeeProfile = mock(Profile.class);
        Profile followerProfile = mock(Profile.class);

        given(profileRepository.findByUserId(eq(followeeId)))
            .willReturn(Optional.of(followeeProfile));
        given(profileRepository.findByUserId(eq(followerId)))
            .willReturn(Optional.of(followerProfile));

        Follow follow = new Follow(followeeId, followerId);
        ReflectionTestUtils.setField(follow, "id", followId);
        given(followRepository.save(any(Follow.class))).willReturn(follow);

        AuthorDto followeeDto = mock(AuthorDto.class);
        AuthorDto followerDto = mock(AuthorDto.class);

        given(profileMapper.toAuthorDto(followeeProfile)).willReturn(followeeDto);
        given(profileMapper.toAuthorDto(followerProfile)).willReturn(followerDto);

        // when
        FollowDto result = followService.follow(followerId, followeeId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(followId);
        assertThat(result.followee()).isSameAs(followeeDto);
        assertThat(result.follower()).isSameAs(followerDto);

        then(profileRepository).should().findByUserId(followeeId);
        then(profileRepository).should().findByUserId(followerId);
        then(followRepository).should().save(any(Follow.class));
        then(profileMapper).should().toAuthorDto(followeeProfile);
        then(profileMapper).should().toAuthorDto(followerProfile);
        // 알림 이벤트 발행 검증
        verify(eventPublisher).publishEvent(any(FollowCreatedEvent.class));
    }

    @Test
    @DisplayName("팔로우 등록 실패 - followee 프로필 없음")
    void createFollow_fail_followeeNotFound() {
        // given
        given(profileRepository.findByUserId(eq(followeeId)))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> followService.follow(followerId, followeeId))
            .isInstanceOf(ProfileNotFoundException.class)
            .hasMessage("존재하지 않는 프로필 입니다.");

        then(profileRepository).should(times(1)).findByUserId(followeeId);
        then(profileRepository).should(never()).findByUserId(followerId);
        then(followRepository).shouldHaveNoInteractions();
        then(profileMapper).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("팔로우 취소 성공")
    void unFollow_shouldDeleteFollow() {
        // given
        Follow follow = mock(Follow.class);
        given(followRepository.findById(followId)).willReturn(Optional.of(follow));
        given(follow.getFollowerId()).willReturn(currentUserId);

        // when
        followService.unFollow(followId, currentUserId);

        // then
        verify(followRepository).deleteById(followId);
    }

    @Test
    @DisplayName("팔로우 취소 실패 - 존재하지 않는 followId일 경우 예외 발생")
    void unFollow_shouldThrowException_whenNotExists() {
        // given
        given(followRepository.findById(followId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> followService.unFollow(followId, currentUserId))
            .isInstanceOf(FollowNotFoundException.class);

        verify(followRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("팔로우 취소 실패 - 다른 사용자가 요청한 경우 예외 발생")
    void unFollow_shouldThrowException_whenNotOwner() {
        // given
        Follow follow = mock(Follow.class);
        given(followRepository.findById(followId)).willReturn(Optional.of(follow));
        given(follow.getFollowerId()).willReturn(UUID.randomUUID());

        // when & then
        assertThatThrownBy(() -> followService.unFollow(followId, currentUserId))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("본인의 팔로우만 취소할 수 있습니다.");

        verify(followRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("팔로우 취소 실패 - 이미 삭제된 경우 예외 발생 (동시성 제어)")
    void unFollow_shouldThrowException_whenAlreadyDeleted() {
        // given
        Follow follow = mock(Follow.class);
        given(followRepository.findById(followId)).willReturn(Optional.of(follow));
        given(follow.getFollowerId()).willReturn(currentUserId);

        doThrow(new EmptyResultDataAccessException(1))
            .when(followRepository).deleteById(followId);

        // when & then
        assertThatThrownBy(() -> followService.unFollow(followId, currentUserId))
            .isInstanceOf(FollowAlreadyDeletedException.class);

        verify(followRepository).deleteById(followId);
    }
}