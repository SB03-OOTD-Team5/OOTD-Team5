package com.sprint.ootd5team.domain.notification.respository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.ootd5team.domain.notification.entity.Notification;
import com.sprint.ootd5team.domain.notification.entity.QNotification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Notification> findByUserWithCursor(
        UUID userId,
        Instant cursor,
        UUID idAfter,
        int size,
        Direction direction
    ) {

        QNotification n = QNotification.notification;

        BooleanBuilder where = new BooleanBuilder()
            .and(n.receiver.id.eq(userId));

        if (cursor != null) {
            if (direction == Sort.Direction.DESC) {
                where.and(
                    n.createdAt.lt(cursor)
                        .or(n.createdAt.eq(cursor).and(n.id.lt(idAfter)))
                );
            } else {
                where.and(
                    n.createdAt.gt(cursor)
                        .or(n.createdAt.eq(cursor).and(n.id.gt(idAfter)))
                );
            }
        }

        return queryFactory.selectFrom(n)
            .where(where)
            .orderBy(
                direction == Sort.Direction.DESC ? n.createdAt.desc() : n.createdAt.asc(),
                direction == Sort.Direction.DESC ? n.id.desc() : n.id.asc()
            )
            .limit(size + 1) // hasNext 판단용
            .fetch();
    }

}
