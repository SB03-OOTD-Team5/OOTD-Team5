package com.sprint.ootd5team.domain.clothes.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.entity.QClothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public List<Clothes> findClothes(UUID ownerId, ClothesType type, String cursor, UUID idAfter,
        int limit) {
        QClothes clothes = QClothes.clothes;

        BooleanBuilder where = new BooleanBuilder();
        where.and(clothes.owner.id.eq(ownerId));

        if (type != null) {
            where.and(clothes.type.eq(type));
            log.debug("[ClothesRepository] type 필터 적용: {}", type);
        }

        if (cursor != null) {
            Instant cursorTime = Instant.parse(cursor);

            BooleanBuilder cursorCondition = new BooleanBuilder();
            cursorCondition.or(clothes.createdAt.lt(cursorTime));

            if (idAfter != null) {
                cursorCondition.or(clothes.createdAt.eq(cursorTime)
                    .and(clothes.id.lt(idAfter)));
                log.debug("[ClothesRepository] idAfter 필터 적용: id < {}", idAfter);
            }

            where.and(cursorCondition);
            log.debug("[ClothesRepository] cursor 필터 적용: createdAt < {}", cursorTime);
        }

        log.info("[ClothesRepository] 옷 목록 조회 실행: ownerId={}, type={}, cursor={}, idAfter={}, limit={}",
            ownerId, type, cursor, idAfter, limit);

        List<Clothes> result = queryFactory
            .selectFrom(clothes)
            .where(where)
            .orderBy(clothes.createdAt.desc(), clothes.id.desc())
            .limit(limit)
            .fetch();

        log.info("[ClothesRepository] 조회 결과: {}건", result.size());
        return result;
    }
}
