package com.sprint.ootd5team.domain.feed.repository.feedClothes.impl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.ootd5team.domain.clothes.entity.QClothes;
import com.sprint.ootd5team.domain.feed.dto.data.OotdDto;
import com.sprint.ootd5team.domain.feed.entity.QFeedClothes;
import com.sprint.ootd5team.domain.feed.repository.feedClothes.FeedClothesRepositoryCustom;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FeedClothesRepositoryImpl implements FeedClothesRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<UUID, List<OotdDto>> findOotdsByFeedIds(List<UUID> feedIds) {
        QFeedClothes feedClothes = QFeedClothes.feedClothes;
        QClothes clothes = QClothes.clothes;

        List<Tuple> rows = queryFactory
            .select(feedClothes.feedId,
                Projections.constructor(OotdDto.class,
                    clothes.id,
                    clothes.name,
                    clothes.imageUrl,
                    clothes.type.stringValue(),
                    Expressions.constant(List.of()) // attributes는 연관 기능 구현 후 수정
                )
            )
            .from(feedClothes)
            .join(clothes).on(feedClothes.clothesId.eq(clothes.id))
            .where(feedClothes.feedId.in(feedIds))
            .fetch();

        return rows.stream().collect(Collectors.groupingBy(
            t -> t.get(feedClothes.feedId),
            Collectors.mapping(t -> t.get(1, OotdDto.class), Collectors.toList())
        ));
    }
}