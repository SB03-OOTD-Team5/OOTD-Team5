package com.sprint.ootd5team.follow.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.ootd5team.base.config.JpaAuditingConfig;
import com.sprint.ootd5team.base.config.QuerydslConfig;
import com.sprint.ootd5team.domain.follow.dto.data.FollowProjectionDto;
import com.sprint.ootd5team.domain.follow.dto.data.FollowSummaryDto;
import com.sprint.ootd5team.domain.follow.dto.enums.FollowDirection;
import com.sprint.ootd5team.domain.follow.entity.Follow;
import com.sprint.ootd5team.domain.follow.repository.impl.FollowRepositoryImpl;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({QuerydslConfig.class, JpaAuditingConfig.class})
@ActiveProfiles("test")
@DisplayName("FollowRepositoryImpl 테스트")
public class FollowRepositoryImplTest {

    @Autowired
    private FollowRepositoryImpl followRepositoryImpl;

    @Autowired
    private TestEntityManager em;

    private UUID followerId;
    private UUID followee1Id;
    private UUID followee2Id;

    @BeforeEach
    void setUp() throws InterruptedException {

        User follower = new User("follower","test1@test.com","password", Role.USER);
        User user1 = new User("followee1","test2@test.com","password", Role.USER);
        User user2 = new User("followee2","test3@test.com","password", Role.USER);

        em.persist(follower);
        em.persist(user1);
        em.persist(user2);

        followerId = follower.getId();
        followee1Id = user1.getId();
        followee2Id = user2.getId();


        em.flush();

        Profile followerProfile = new Profile(
            follower,
            "follower",
            null,
            null,
            null,
            null,
            2
        );
        em.persist(followerProfile);

        Profile followee1 = new Profile(
            user1, "user1", null, null, null, null, 2
        );
        em.persist(followee1);

        Profile followee2 = new Profile(
            user2, "user2", null, null, null, null, 2
        );
        em.persist(followee2);

        em.flush();

        Follow f1 = new Follow(followee1Id, followerId);
        em.persist(f1);

        Thread.sleep(5);

        Follow f2 = new Follow(followee2Id, followerId);
        em.persist(f2);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("팔로잉 목록 조회 - 기본 조회 (최신순 정렬, limit 동작)")
    void findByCursor_following_basic() {
        List<FollowProjectionDto> results = followRepositoryImpl.findByCursor(
            followerId,
            null,
            null,
            10,
            null,
            FollowDirection.FOLLOWING
        );

        assertThat(results).hasSize(2);
        assertThat(results.get(0).followee().name()).isEqualTo("user2");
        assertThat(results.get(1).followee().name()).isEqualTo("user1");
    }

    @Test
    @DisplayName("팔로잉 목록 조회 - cursor 조건 적용")
    void findByCursor_following_withCursor() {
        List<FollowProjectionDto> firstPage = followRepositoryImpl.findByCursor(
            followerId,
            null,
            null,
            1,
            null,
            FollowDirection.FOLLOWING
        );
        FollowProjectionDto last = firstPage.get(0);

        List<FollowProjectionDto> secondPage = followRepositoryImpl.findByCursor(
            followerId,
            last.createdAt(),
            last.id(),
            1,
            null,
            FollowDirection.FOLLOWING
        );

        assertThat(secondPage).extracting(dto -> dto.followee().name())
            .containsExactly("user1");
    }

    @Test
    @DisplayName("팔로잉 목록 조회 - nameLike 조건 적용")
    void findByCursor_following_nameLike() {
        List<FollowProjectionDto> results = followRepositoryImpl.findByCursor(
            followerId,
            null,
            null,
            10,
            "user1",
            FollowDirection.FOLLOWING
        );

        assertThat(results).hasSize(1);
        assertThat(results.get(0).followee().name()).isEqualTo("user1");
    }

    @Test
    @DisplayName("팔로잉 수 조회 - nameLike 조건 없이 전체 카운트")
    void countByUserIdAndNameLike_noFilter() {
        long count = followRepositoryImpl.countByUserIdAndNameLike(
            followerId,
            null,
            FollowDirection.FOLLOWING
        );

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("팔로잉 수 조회 - nameLike 조건 적용 (대소문자 무시)")
    void countByUserIdAndNameLike_withFilter() {
        long count = followRepositoryImpl.countByUserIdAndNameLike(
            followerId,
            "USER1",
            FollowDirection.FOLLOWING
        );

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("팔로잉 수 조회 - nameLike 조건에 해당하는 사용자가 없을 때 0 반환")
    void countByUserIdAndNameLike_noMatch() {
        long count = followRepositoryImpl.countByUserIdAndNameLike(
            followerId,
            "zzz",
            FollowDirection.FOLLOWING
        );

        assertThat(count).isZero();
    }

    @Test
    @DisplayName("팔로워 목록 조회 - 기본 조회 (최신순 정렬, limit 동작)")
    void findByCursor_follower_basic() {
        List<FollowProjectionDto> results = followRepositoryImpl.findByCursor(
            followee1Id,
            null,
            null,
            10,
            null,
            FollowDirection.FOLLOWER
        );

        assertThat(results).hasSize(1);
        assertThat(results.get(0).follower().name()).isEqualTo("follower");
    }

    @Test
    @DisplayName("팔로워 목록 조회 - cursor 조건 적용")
    void findByCursor_follower_withCursor() {
        List<FollowProjectionDto> firstPage = followRepositoryImpl.findByCursor(
            followee1Id,
            null,
            null,
            1,
            null,
            FollowDirection.FOLLOWER
        );
        assertThat(firstPage).isNotEmpty();

        FollowProjectionDto last = firstPage.get(0);

        List<FollowProjectionDto> secondPage = followRepositoryImpl.findByCursor(
            followee1Id,
            last.createdAt(),
            last.id(),
            1,
            null,
            FollowDirection.FOLLOWER
        );

        assertThat(secondPage).isEmpty();
    }

    @Test
    @DisplayName("팔로워 목록 조회 - nameLike 조건 적용")
    void findByCursor_follower_nameLike() {
        List<FollowProjectionDto> results = followRepositoryImpl.findByCursor(
            followee2Id,
            null,
            null,
            10,
            "follower",
            FollowDirection.FOLLOWER
        );

        assertThat(results).hasSize(1);
        assertThat(results.get(0).follower().name()).isEqualTo("follower");
    }

    @Test
    @DisplayName("팔로워 수 조회 - nameLike 없이 전체 카운트")
    void countByUserIdAndNameLike_follower_noFilter() {
        long count = followRepositoryImpl.countByUserIdAndNameLike(
            followee1Id,
            null,
            FollowDirection.FOLLOWER
        );

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("팔로워 수 조회 - nameLike 조건 적용")
    void countByUserIdAndNameLike_follower_withFilter() {
        long count = followRepositoryImpl.countByUserIdAndNameLike(
            followee1Id,
            "follower",
            FollowDirection.FOLLOWER
        );

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("팔로워 수 조회 - nameLike 조건 불일치시 0 반환")
    void countByUserIdAndNameLike_follower_noMatch() {
        long count = followRepositoryImpl.countByUserIdAndNameLike(
            followee1Id,
            "zzz",
            FollowDirection.FOLLOWER
        );

        assertThat(count).isZero();
    }

    @Test
    @DisplayName("팔로우 요약 정보 조회 - follower/following/상호관계 검증")
    void getSummary_shouldReturnCorrectSummary() {
        // given
        Follow testFollow = new Follow(followerId, followee1Id);
        em.persist(testFollow);
        em.flush();
        em.clear();

        // when
        FollowSummaryDto summary = followRepositoryImpl.getSummary(followee1Id, followerId);

        // then
        assertThat(summary).isNotNull();
        assertThat(summary.followeeId()).isEqualTo(followee1Id);
        assertThat(summary.followerCount()).isEqualTo(1L);
        assertThat(summary.followingCount()).isEqualTo(1L);
        assertThat(summary.followedByMe()).isTrue();
        assertThat(summary.followedByMeId()).isNotNull();
        assertThat(summary.followingMe()).isTrue();
    }
}