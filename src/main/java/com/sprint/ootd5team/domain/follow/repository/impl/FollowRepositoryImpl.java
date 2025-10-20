package com.sprint.ootd5team.domain.follow.repository.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.ootd5team.domain.follow.dto.data.FollowProjectionDto;
import com.sprint.ootd5team.domain.follow.dto.data.FollowSummaryDto;
import com.sprint.ootd5team.domain.follow.dto.enums.FollowDirection;
import com.sprint.ootd5team.domain.follow.entity.QFollow;
import com.sprint.ootd5team.domain.follow.repository.FollowRepositoryCustom;
import com.sprint.ootd5team.domain.profile.entity.QProfile;
import com.sprint.ootd5team.domain.user.dto.AuthorDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 특정 사용자의 팔로잉 목록을 커서 기반 페이지네이션으로 조회한다.
     *
     * <ul>
     *   <li>항상 {@code follower_id = followerId} 인 행만 조회한다.</li>
     *   <li>커서 조건을 기준으로 무한 스크롤 페이지네이션을 처리한다.</li>
     *   <li>정렬 기준: {@code createdAt DESC}, {@code id DESC}</li>
     * </ul>
     *
     * @param userId        조회할 사용자 ID
     * @param createdCursor 커서 비교용 생성 시각
     * @param idCursor      커서 비교용 팔로우 ID
     * @param limit         조회할 최대 건수
     * @param nameLike      팔로우 대상자의 이름 검색 키워드 (대소문자 구분 없음), null 또는 공백일 경우 무시됨
     * @return              FollowProjectionDto (최대 {@code limit} + 1 건)
     */
    @Override
    public List<FollowProjectionDto> findByCursor(
        UUID userId,
        Instant createdCursor,
        UUID idCursor,
        int limit,
        String nameLike,
        FollowDirection direction
    ) {
        QFollow follow = QFollow.follow;
        QProfile followeeProfile = new QProfile("followeeProfile");
        QProfile followerProfile = new QProfile("followerProfile");

        BooleanBuilder where = buildCursorWhere(follow, userId, createdCursor, idCursor, nameLike, direction);

        return queryFactory
            .select(Projections.constructor(FollowProjectionDto.class,
                follow.id,
                follow.createdAt,
                Projections.constructor(AuthorDto.class,
                    followeeProfile.user.id,
                    followeeProfile.name,
                    followeeProfile.profileImageUrl
                ),
                Projections.constructor(AuthorDto.class,
                    followerProfile.user.id,
                    followerProfile.name,
                    followerProfile.profileImageUrl
                )
            ))
            .from(follow)
            .join(followeeProfile).on(follow.followeeId.eq(followeeProfile.user.id))
            .join(followerProfile).on(follow.followerId.eq(followerProfile.user.id))
            .where(where)
            .orderBy(follow.createdAt.desc(), follow.id.desc())
            .limit(limit + 1)
            .fetch();
    }

    /**
     * 주어진 사용자 ID를 기준으로 팔로잉/팔로워 수를 계산한다.
     *
     * <p>조회 대상은 {@link FollowDirection} 으로 결정</p>
     *
     * <ul>
     *   <li>{@link FollowDirection#FOLLOWING}:
     *       - {@code userId}는 followerId로 사용된다.
     *       - 사용자가 팔로우하고 있는 대상(followee)의 수를 센다.
     *   </li>
     *   <li>{@link FollowDirection#FOLLOWER}:
     *       - {@code userId}는 followeeId로 사용된다.
     *       - 사용자를 팔로우하고 있는 사람(follower)의 수를 센다.
     *   </li>
     * </ul>
     *
     * @param userId    사용자 ID (followerId / followeeId)
     * @param nameLike  프로필 이름 검색 키워드 (null 또는 공백이면 무시)
     * @param direction 조회 방향 (FOLLOWING / FOLLOWER)
     * @return          조건에 해당하는 팔로우 수, 없을 경우 0
     */
    @Override
    public long countByUserIdAndNameLike(UUID userId, String nameLike, FollowDirection direction) {
        QFollow follow = QFollow.follow;
        QProfile followeeProfile = new QProfile("followeeProfile");
        QProfile followerProfile = new QProfile("followerProfile");

        BooleanBuilder where = new BooleanBuilder();

        log.debug("[FollowRepositoryCustom] 팔로우 수 조회 대상 - Direction:{}", direction.name());

        if (direction == FollowDirection.FOLLOWING) {
            where.and(follow.followerId.eq(userId));
            if (nameLike != null && !nameLike.isBlank()) {
                where.and(followeeProfile.name.containsIgnoreCase(nameLike));
            }
        } else {
            where.and(follow.followeeId.eq(userId));
            if (nameLike != null && !nameLike.isBlank()) {
                where.and(followerProfile.name.containsIgnoreCase(nameLike));
            }
        }

        Long count = queryFactory
            .select(follow.count())
            .from(follow)
            .join(followeeProfile).on(follow.followeeId.eq(followeeProfile.user.id))
            .join(followerProfile).on(follow.followerId.eq(followerProfile.user.id))
            .where(where)
            .fetchOne();
        
        log.debug("[FollowRepositoryCustom] 집계된 팔로우 수 - count:{}", count);

        return count != null ? count : 0L;
    }

    @Override
    public FollowSummaryDto getSummary(UUID userId, UUID currentUserId) {
        QFollow follow = QFollow.follow;

        Long followerCount = queryFactory
            .select(follow.count())
            .from(follow)
            .where(follow.followeeId.eq(userId))
            .fetchOne();

        Long followingCount = queryFactory
            .select(follow.count())
            .from(follow)
            .where(follow.followerId.eq(userId))
            .fetchOne();

        boolean isFollowedByMe = queryFactory
            .selectFrom(follow)
            .where(follow.followeeId.eq(userId)
                .and(follow.followerId.eq(currentUserId)))
            .fetchFirst() != null;

        UUID followId = queryFactory
            .select(follow.id)
            .from(follow)
            .where(follow.followeeId.eq(userId)
                .and(follow.followerId.eq(currentUserId)))
            .fetchOne();

        boolean isFollowingMe = queryFactory
            .selectFrom(follow)
            .where(follow.followeeId.eq(currentUserId)
                .and(follow.followerId.eq(userId)))
            .fetchFirst() != null;

        log.debug("[FollowRepositoryCustom] 요약된 팔로우 정보 - "
                + "followerCount:{}, followingCount:{}, isFollowedByMe:{}, followId:{}, isFollowingMe:{}",
            followerCount, followingCount, isFollowedByMe, followId, isFollowingMe);

        return new FollowSummaryDto(
            userId, followerCount, followingCount, isFollowedByMe, followId, isFollowingMe
        );
    }

    /**
     * 커서 기반 팔로우 목록 조회 조건을 생성한다.
     *
     * <p>조회 대상은 {@link FollowDirection} 으로 결정</p>
     *
     * <ul>
     *   <li>{@link FollowDirection#FOLLOWING}:
     *       - {@code userId}는 followerId로 사용된다.
     *       - 사용자가 팔로우하고 있는 대상(followee)을 조회한다.
     *   </li>
     *   <li>{@link FollowDirection#FOLLOWER}:
     *       - {@code userId}는 followeeId로 사용된다.
     *       - 사용자를 팔로우하는 사람(follower)을 조회한다.
     *   </li>
     * </ul>
     *
     * <p>커서 기반 조건:
     * <ul>
     *   <li>{@code createdAt < createdCursor} 또는 {@code createdAt = createdCursor && id < idCursor}</li>
     *   <li>{@code nameLike} 값이 존재하면 대소문자를 무시한 필터링 조건 추가</li>
     * </ul>
     *
     * @param follow        참조 QFollow 엔티티
     * @param userId        사용자 ID (followerId / followeeId)
     * @param createdCursor 커서 비교용 생성 시각
     * @param idCursor      커서 비교용 팔로우 ID
     * @param nameLike      프로필 이름 검색 키워드 (null 또는 공백이면 무시)
     * @param direction     조회 대상 (FOLLOWING / FOLLOWER)
     * @return              QueryDSL BooleanBuilder 조건
     */
    private BooleanBuilder buildCursorWhere(
        QFollow follow,
        UUID userId,
        Instant createdCursor,
        UUID idCursor,
        String nameLike,
        FollowDirection direction
    ) {
        log.debug("[FollowRepositoryCustom] 페이지네이션 조회 대상 - Direction:{}", direction.name());

        BooleanBuilder where = new BooleanBuilder();

        if (direction == FollowDirection.FOLLOWING) {
            where.and(follow.followerId.eq(userId));
        } else {
            where.and(follow.followeeId.eq(userId));
        }

        if (createdCursor != null && idCursor != null) {
            where.and(
                follow.createdAt.lt(createdCursor)
                    .or(follow.createdAt.eq(createdCursor)
                        .and(follow.id.lt(idCursor)))
            );
        }

        if (nameLike != null && !nameLike.isBlank()) {
            if (direction == FollowDirection.FOLLOWING) {
                QProfile followeeProfile = new QProfile("followeeProfile");
                where.and(followeeProfile.name.containsIgnoreCase(nameLike));
            } else {
                QProfile followerProfile = new QProfile("followerProfile");
                where.and(followerProfile.name.containsIgnoreCase(nameLike));
            }
        }

        return where;
    }
}