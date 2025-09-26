package com.sprint.ootd5team.domain.comment.repository.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.ootd5team.domain.comment.dto.data.CommentDto;
import com.sprint.ootd5team.domain.comment.entity.QFeedComment;
import com.sprint.ootd5team.domain.comment.repository.FeedCommentRepositoryCustom;
import com.sprint.ootd5team.domain.profile.entity.QProfile;
import com.sprint.ootd5team.domain.user.dto.AuthorDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FeedCommentRepositoryImpl implements FeedCommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public List<CommentDto> findByFeedIdWithCursor(
        UUID feedId,
        Instant createdAtCursor,
        UUID idCursor,
        int pageSize
    ) {
        QFeedComment comment = QFeedComment.feedComment;
        QProfile profile = QProfile.profile;

        return queryFactory
            .select(Projections.constructor(CommentDto.class,
                comment.id,
                comment.createdAt,
                comment.feedId,
                Projections.constructor(AuthorDto.class,
                    profile.user.id,
                    profile.name,
                    profile.profileImageUrl
                ),
                comment.content
            ))
            .from(comment)
            .leftJoin(profile).on(comment.authorId.eq(profile.user.id))
            .where(
                comment.feedId.eq(feedId),
                cursorPredicate(comment, createdAtCursor, idCursor)
            )
            .orderBy(comment.createdAt.desc(), comment.id.desc())
            .limit(pageSize + 1)
            .fetch();
    }

    private BooleanExpression cursorPredicate(QFeedComment comment, Instant createdAtCursor, UUID idCursor) {
        if (createdAtCursor == null || idCursor == null) {
            return null;
        }
        return comment.createdAt.lt(createdAtCursor)
            .or(comment.createdAt.eq(createdAtCursor).and(comment.id.lt(idCursor)));
    }
}