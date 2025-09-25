package com.sprint.ootd5team.domain.follow.repository.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.ootd5team.domain.follow.dto.data.FollowProjectionDto;
import com.sprint.ootd5team.domain.follow.entity.QFollow;
import com.sprint.ootd5team.domain.follow.repository.FollowRepositoryCustom;
import com.sprint.ootd5team.domain.profile.entity.QProfile;
import com.sprint.ootd5team.domain.user.dto.AuthorDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 특정 사용자의 팔로잉 목록을 커서 기반 페이지네이션으로 조회한다.
     *
     * <ul>
     *   <li>항상 {@code follower_id = followerId} 인 행만 조회한다.</li>
     *   <li>커서 조건을 기준으로 무한 스크롤 페이지네이션을 처리한다.</li>
     *   <li>{@code nameLike} 값이 존재할 경우,
     *       팔로우 대상자(followee)의 이름에 해당 문자열이 포함되는 경우(대소문자 무시)로 추가 필터링한다.</li>
     *   <li>정렬 기준: {@code createdAt DESC}, {@code id DESC}</li>
     *   <li>조회 건수는 요청한 {@code limit} + 1 로 제한하여 hasNext 여부 판단이 가능하다.</li>
     * </ul>
     *
     * @param followerId    팔로잉 목록을 조회할 사용자 ID
     * @param createdCursor 커서 비교용 생성 시각
     * @param idCursor      커서 비교용 팔로우 ID
     * @param limit         조회할 최대 건수
     * @param nameLike      팔로우 대상자의 이름 검색 키워드 (대소문자 구분 없음), null 또는 공백일 경우 무시됨
     * @return FollowProjectionDto (최대 {@code limit} + 1 건)
     */
    @Override
    public List<FollowProjectionDto> findByFollowIdWithCursor(
        UUID followerId,
        Instant createdCursor,
        UUID idCursor,
        int limit,
        String nameLike
    ) {
        QFollow follow = QFollow.follow;
        QProfile followeeProfile = new QProfile("followeeProfile");
        QProfile followerProfile = new QProfile("followerProfile");

        BooleanBuilder where = new BooleanBuilder();
        where.and(follow.followerId.eq(followerId));

        if (createdCursor != null && idCursor != null) {
            where.and(
                follow.createdAt.lt(createdCursor)
                    .or(follow.createdAt.eq(createdCursor)
                        .and(follow.id.lt(idCursor)))
            );
        }

        if (nameLike != null && !nameLike.isBlank()) {
            where.and(followeeProfile.name.containsIgnoreCase(nameLike));
        }

        return queryFactory
            .select(Projections.constructor(FollowProjectionDto.class,
                follow.id,
                follow.createdAt,
                Projections.constructor(AuthorDto.class,
                    followeeProfile.userId,
                    followeeProfile.name,
                    followeeProfile.profileImageUrl
                ),
                Projections.constructor(AuthorDto.class,
                    followerProfile.userId,
                    followerProfile.name,
                    followerProfile.profileImageUrl
                )
            ))
            .from(follow)
            .join(followeeProfile).on(follow.followeeId.eq(followeeProfile.userId))
            .join(followerProfile).on(follow.followerId.eq(followerProfile.userId))
            .where(where)
            .orderBy(follow.createdAt.desc(), follow.id.desc())
            .limit(limit + 1)
            .fetch();
    }

    /**
     * 특정 사용자가 팔로우한 대상(팔로잉) 수를 조회한다.
     *
     * <ul>
     *   <li>항상 {@code follower_id = followerId} 인 행만 조회한다.</li>
     *   <li>{@code nameLike} 값이 null 이 아니고 공백이 아닐 경우,
     *       팔로우 대상자의 이름이 {@code nameLike} 문자열을 포함(대소문자 무시)하는 경우로 필터링한다.</li>
     *   <li>조회 결과가 없을 경우 0을 반환한다.</li>
     * </ul>
     *
     * @param followerId 팔로우를 수행한 사용자 ID
     * @param nameLike   팔로우 대상자의 이름 검색 키워드 (대소문자 구분 없음)
     * @return 조건에 해당하는 팔로잉 수, 없을 경우 0
     */
    @Override
    public long countByFollowerIdAndNameLike(UUID followerId, String nameLike) {
        QFollow follow = QFollow.follow;
        QProfile followeeProfile = new QProfile("followeeProfile");

        Long count = queryFactory
            .select(follow.count())
            .from(follow)
            .join(followeeProfile).on(follow.followeeId.eq(followeeProfile.userId))
            .where(
                follow.followerId.eq(followerId),
                nameLike != null && !nameLike.isBlank()
                    ? followeeProfile.name.containsIgnoreCase(nameLike)
                    : null
            )
            .fetchOne();

        return count != null ? count : 0L;
    }
}