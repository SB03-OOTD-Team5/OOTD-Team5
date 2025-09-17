package com.sprint.ootd5team.domain.feed.repository.feedClothes.impl;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.clothattribute.entity.QClothesAttribute;
import com.sprint.ootd5team.domain.clothattribute.entity.QClothesAttributeDef;
import com.sprint.ootd5team.domain.clothattribute.entity.QClothesAttributeValue;
import com.sprint.ootd5team.domain.clothes.entity.QClothes;
import com.sprint.ootd5team.domain.feed.dto.data.OotdDto;
import com.sprint.ootd5team.domain.feed.entity.QFeedClothes;
import com.sprint.ootd5team.domain.feed.repository.feedClothes.FeedClothesRepositoryCustom;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FeedClothesRepositoryImpl implements FeedClothesRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 주어진 feedId 목록에 속한 모든 의상(OOTD)들을 조회한다.
     * <p>
     * 조회 시 다음과 같은 연관 엔티티들을 join 하여 한 번에 DTO로 매핑한다:
     * <ul>
     *   <li>{@code FeedClothes} → 피드와 의상 매핑</li>
     *   <li>{@code Clothes} → 의상 기본 정보</li>
     *   <li>{@code ClothesAttributeValue} → 의상에 매핑된 속성값</li>
     *   <li>{@code ClothesAttribute} → 속성 정의(예: 소재, 계절)</li>
     *   <li>{@code ClothesAttributeDef} → 속성의 selectable value 목록</li>
     * </ul>
     *
     * @param feedIds 조회할 피드 ID 목록
     * @return Map<UUID, List<OotdDto>>
     */
    @Override
    public Map<UUID, List<OotdDto>> findOotdsByFeedIds(List<UUID> feedIds) {
        QFeedClothes feedClothes = QFeedClothes.feedClothes;
        QClothes clothes = QClothes.clothes;
        QClothesAttribute attribute = QClothesAttribute.clothesAttribute;
        QClothesAttributeValue attrValue = QClothesAttributeValue.clothesAttributeValue;
        QClothesAttributeDef attrDef = QClothesAttributeDef.clothesAttributeDef;

        List<Tuple> rows = queryFactory
            .select(feedClothes.feedId,
                clothes.id,
                clothes.name,
                clothes.imageUrl,
                clothes.type,
                attribute.id,
                attribute.name,
                attrDef.id,
                attrDef.attDef,
                attrValue.defValue)
            .from(feedClothes)
            .join(clothes).on(feedClothes.clothesId.eq(clothes.id))
            .leftJoin(attrValue).on(attrValue.clothes.id.eq(clothes.id))
            .leftJoin(attribute).on(attrValue.attribute.id.eq(attribute.id))
            .leftJoin(attrDef).on(attrDef.attribute.id.eq(attribute.id))
            .where(feedClothes.feedId.in(feedIds))
            .fetch();

        return rows.stream()
            .collect(Collectors.groupingBy(
                row -> Objects.requireNonNull(row.get(feedClothes.feedId)),
                Collectors.collectingAndThen(Collectors.toList(), list -> {
                    Map<UUID, List<Tuple>> byClothes = list.stream()
                        .collect(Collectors.groupingBy(r -> r.get(clothes.id)));

                    return byClothes.values().stream().map(clothesRows -> {
                        Tuple first = clothesRows.get(0);

                        Map<UUID, List<Tuple>> byDefinition = clothesRows.stream()
                            .filter(r -> r.get(attribute.id) != null)
                            .collect(Collectors.groupingBy(r -> r.get(attribute.id)));

                        List<ClothesAttributeWithDefDto> attributes = byDefinition.values().stream()
                            .map(attrRows -> {
                                Tuple any = attrRows.get(0);
                                String definitionName = any.get(attribute.name);

                                List<String> selectableValues = attrRows.stream()
                                    .map(r -> r.get(attrDef.attDef))
                                    .filter(Objects::nonNull)
                                    .distinct()
                                    .toList();

                                UUID definitionId = attrRows.stream()
                                    .map(r -> r.get(attrDef.id))
                                    .filter(Objects::nonNull)
                                    .findFirst()
                                    .orElse(null);

                                String selectedValue = attrRows.stream()
                                    .map(r -> r.get(attrValue.defValue))
                                    .filter(Objects::nonNull)
                                    .findFirst()
                                    .orElse(null);

                                return new ClothesAttributeWithDefDto(
                                    definitionId,
                                    definitionName,
                                    selectableValues,
                                    selectedValue
                                );
                            })
                            .toList();

                        return new OotdDto(
                            first.get(clothes.id),
                            first.get(clothes.name),
                            first.get(clothes.imageUrl),
                            Objects.requireNonNull(first.get(clothes.type)).toString(),
                            attributes
                        );
                    }).toList();
                })
            ));
    }
}