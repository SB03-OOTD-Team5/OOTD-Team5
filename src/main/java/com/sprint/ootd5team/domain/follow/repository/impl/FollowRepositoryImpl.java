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
}