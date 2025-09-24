package com.sprint.ootd5team.domain.clothes.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.entity.QClothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/**
 * ClothesRepositoryCustom 구현체.
 * <p>
 * QueryDSL을 사용해 옷 목록을 조회한다.
 * - ownerId, type 필터
 * - cursor(createdAt), idAfter(UUID) 기반 페이지네이션 지원
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ClothesRepositoryImpl implements ClothesRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 옷 목록 조회 (커서 기반 페이지네이션).
     *
     * @param ownerId 소유자 UUID
     * @param type    옷 종류 필터 (nullable)
     * @param cursor  커서(createdAt) (nullable)
     * @param idAfter 보조 커서(UUID) (nullable)
     * @param limit   조회 개수
     * @return 조회된 옷 목록
     */
    @Override
    public List<Clothes> findByOwnerWithCursor(
        UUID ownerId,
        ClothesType type,
        Instant cursor,
        UUID idAfter,
        int limit,
        Sort.Direction sortDirection
    ) {
        QClothes clothes = QClothes.clothes;

        BooleanBuilder where = new BooleanBuilder();
        where.and(clothes.owner.id.eq(ownerId));

        if (type != null) {
            where.and(clothes.type.eq(type));
            log.debug("[ClothesRepository] type 필터 적용: {}", type);
        }

        if (cursor != null) {
            BooleanBuilder cursorCondition = new BooleanBuilder();
            if (sortDirection == Sort.Direction.ASC) {
                cursorCondition.and(clothes.createdAt.gt(cursor));
                if (idAfter != null) {
                    cursorCondition.or(clothes.createdAt.eq(cursor).and(clothes.id.gt(idAfter)));
                    log.debug("[ClothesRepository] idAfter 필터 적용(ASC): id > {}", idAfter);
                }
                log.debug("[ClothesRepository] cursor 필터 적용(ASC): createdAt > {}", cursor);
            } else {
                cursorCondition.and(clothes.createdAt.lt(cursor));
                if (idAfter != null) {
                    cursorCondition.or(clothes.createdAt.eq(cursor).and(clothes.id.lt(idAfter)));
                    log.debug("[ClothesRepository] idAfter 필터 적용(DESC): id < {}", idAfter);
                }
                log.debug("[ClothesRepository] cursor 필터 적용(DESC): createdAt < {}", cursor);
            }
            where.and(cursorCondition);
        }
        OrderSpecifier<?> createdAtOrder =
            sortDirection == Sort.Direction.ASC ? clothes.createdAt.asc()
                : clothes.createdAt.desc();
        OrderSpecifier<?> idOrder =
            sortDirection == Sort.Direction.ASC ? clothes.id.asc() : clothes.id.desc();

        log.info("[ClothesRepository] 옷 목록 조회 실행: "
                + "ownerId={}, type={}, cursor={}, idAfter={}, limit={}, sortDirection={}",
            ownerId, type, cursor, idAfter, limit, sortDirection.name());

        List<Clothes> result = queryFactory
            .selectFrom(clothes)
            .where(where)
            .orderBy(createdAtOrder, idOrder)
            .limit(limit)
            .fetch();

        log.info("[ClothesRepository] 조회 결과: {}건", result.size());
        return result;
    }
}
