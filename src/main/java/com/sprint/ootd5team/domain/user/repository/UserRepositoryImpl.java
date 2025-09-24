package com.sprint.ootd5team.domain.user.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.ootd5team.domain.user.entity.QUser;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;
    private final QUser user = QUser.user;


    /**
     * 커서 페이지네이션을 통해 User 리스트를 반환하는 메서드
     * @param cursor 커서
     * @param idAfter 다음 페이지 첫번째 항목의 UUID
     * @param limit 한 페이지가 가지는 user의 수
     * @param sortBy 정렬자
     * @param sortDirection 정렬 방향
     * @param emailLike 해당 문자를 가지는 email 검색
     * @param roleEqual ADMIN 인지 USER인지
     * @param locked 계정 잠금 여부
     * @return 페이지네이션으로 검색된 유저 리스트
     */
    @Override
    public List<User> findUsersWithCursor(
        String cursor,
        UUID idAfter,
        Integer limit,
        String sortBy,
        String sortDirection,
        String emailLike,
        String roleEqual,
        Boolean locked) {

        log.debug("findUsersWithCursor 시작");
        Order order = sortDirection.equals("ASCENDING") ? Order.ASC : Order.DESC;
        List<User> fetch = jpaQueryFactory.selectFrom(user)
            .where(
                buildCursorCondition(cursor, idAfter, order, sortBy),
                emailLikeCondition(emailLike),
                lockedCondition(locked),
                roleCondition(roleEqual)
            )
            .orderBy(buildOrderSpecifier(sortBy, order))
            .limit(limit)
            .fetch();

        log.debug("findUsersWithCursor 완료");
        return fetch;

    }

    /**
     * 커서를 기준으로 검색범위를 지정하는 메서드
     * @param cursor 검색하길 원하는 커서
     * @param idAfter  보조 검색자(UUID)
     * @param sortBy  정렬할 필드 (Email, createdAt)
     * @param order 정렬 순서
     * @return  검색 범위 조건
     */
    private BooleanExpression buildCursorCondition(String cursor, UUID idAfter,Order order, String sortBy){
        if(cursor == null || idAfter == null){
            return null;
        }
        switch(sortBy){
            case "email":
                if(order.equals(Order.ASC)){
                    return user.email.goe(cursor);
                }
                else{
                    return user.email.loe(cursor);
                }
            case "createdAt":
                if(order.equals(Order.ASC)){
                    return user.createdAt.goe(Instant.parse(cursor));
                }
                else{
                    return user.createdAt.loe(Instant.parse(cursor));
                }
            default:
                return null;

        }


    }

    /**
     * 정렬 조건 생성 메서드
     *
     * @param sortBy 정렬하길 원하는 필드
     * @param order    정렬방법
     * @return OrderSpecifier
     */
    private OrderSpecifier<?> buildOrderSpecifier(String sortBy, Order order){

        switch (sortBy) {
            case "email":
                return new OrderSpecifier<>(order, user.email);
            case "createdAt":
                return new OrderSpecifier<>(order, user.createdAt);
            default:
                return new OrderSpecifier<>(order, user.email);
        }

    }

    @Override
    public Long countUsers(String role,String emailLike, Boolean locked) {
        log.debug("countUsers 시작");
        Long result = jpaQueryFactory
            .select(user.count())
            .from(user)
            .where(
                lockedCondition(locked),
                roleCondition(role),
                emailLikeCondition(emailLike)
            ).fetchOne();

        log.debug("countUsers 완료 결과:{}", result);
        return result;

    }

    private BooleanExpression lockedCondition(Boolean locked) {
        return locked != null ? user.locked.eq(locked) : null;
    }

    private BooleanExpression roleCondition(String roleEqual) {
        return roleEqual != null ? user.role.eq(Role.valueOf(roleEqual)) : null;
    }
    private BooleanExpression emailLikeCondition(String emailLike) {
        if (emailLike == null || emailLike.isBlank()) {
            return null;
        }
        return user.email.likeIgnoreCase("%" + emailLike + "%");
    }
}
