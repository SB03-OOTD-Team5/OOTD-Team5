package com.sprint.ootd5team.domain.recommendation.engine.model;

import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;

public record ScoredClothes(
    ClothesFilteredDto item,
    double score
) {

}
