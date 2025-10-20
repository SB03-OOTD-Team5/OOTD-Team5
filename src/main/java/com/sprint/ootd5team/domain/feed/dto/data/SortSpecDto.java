package com.sprint.ootd5team.domain.feed.dto.data;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import java.util.List;

public record SortSpecDto(
    List<OrderSpecifier<?>> orderSpecifiers,
    BooleanExpression cursorCondition
) {}