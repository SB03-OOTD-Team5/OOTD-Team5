package com.sprint.ootd5team.follow.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.ootd5team.base.config.JpaAuditingConfig;
import com.sprint.ootd5team.base.config.QuerydslConfig;
import com.sprint.ootd5team.domain.follow.dto.data.FollowProjectionDto;
import com.sprint.ootd5team.domain.follow.entity.Follow;
import com.sprint.ootd5team.domain.follow.repository.impl.FollowRepositoryImpl;
import com.sprint.ootd5team.domain.profile.entity.Profile;
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
        followerId = UUID.randomUUID();
        followee1Id = UUID.randomUUID();
        followee2Id = UUID.randomUUID();

        Profile followerProfile = new Profile(
            followerId,
            "follower",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            2
        );
        em.persist(followerProfile);

        Profile followee1 = new Profile(
            followee1Id, "user1", null, null, null,
            null, null, null, null, null, 2
        );
        em.persist(followee1);

        Profile followee2 = new Profile(
            followee2Id, "user2", null, null, null, null,
            null, null, null, null, 2
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
    @DisplayName("기본 조회 - 최신순 정렬, limit 동작 확인")
    void findByFollowIdWithCursor_basic() {
        List<FollowProjectionDto> results = followRepositoryImpl.findByFollowIdWithCursor(
            followerId,
            null,
            null,
            1,
            null
        );

        assertThat(results).hasSize(2);
        assertThat(results.get(0).followee().name()).isEqualTo("user2");
        assertThat(results.get(1).followee().name()).isEqualTo("user1");
    }

    @Test
    @DisplayName("cursor 조건 적용 - createdAt, id 기반 페이징")
    void findByFollowIdWithCursor_withCursor() {
        List<FollowProjectionDto> firstPage = followRepositoryImpl.findByFollowIdWithCursor(
            followerId,
            null,
            null,
            1,
            null
        );
        FollowProjectionDto last = firstPage.get(0);

        // cursor 로 전달
        List<FollowProjectionDto> secondPage = followRepositoryImpl.findByFollowIdWithCursor(
            followerId,
            last.createdAt(),
            last.id(),
            1,
            null
        );

        assertThat(secondPage).extracting(dto -> dto.followee().name())
            .containsExactly("user1");
    }

    @Test
    @DisplayName("이름 필터 적용 - nameLike")
    void findByFollowIdWithCursor_nameLike() {
        List<FollowProjectionDto> results = followRepositoryImpl.findByFollowIdWithCursor(
            followerId,
            null,
            null,
            10,
            "user1"
        );

        assertThat(results).hasSize(1);
        assertThat(results.get(0).followee().name()).isEqualTo("user1");
    }

    @Test
    @DisplayName("팔로잉 수 조회 - nameLike 조건 없이 전체 카운트")
    void countByFollowerIdAndNameLike_noFilter() {
        long count = followRepositoryImpl.countByFollowerIdAndNameLike(followerId, null);

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("팔로잉 수 조회 - nameLike 조건 적용 (대소문자 무시)")
    void countByFollowerIdAndNameLike_withFilter() {
        long count = followRepositoryImpl.countByFollowerIdAndNameLike(followerId, "USER1");

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("팔로잉 수 조회 - nameLike 조건에 해당하는 사용자가 없을 때 0 반환")
    void countByFollowerIdAndNameLike_noMatch() {
        long count = followRepositoryImpl.countByFollowerIdAndNameLike(followerId, "zzz");

        assertThat(count).isZero();
    }

}