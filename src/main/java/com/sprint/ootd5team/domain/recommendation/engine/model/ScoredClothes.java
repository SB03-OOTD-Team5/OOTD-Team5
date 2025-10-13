package com.sprint.ootd5team.domain.recommendation.engine.model;

import com.sprint.ootd5team.domain.recommendation.dto.RecommendationClothesDto;

public record ScoredClothes(
    RecommendationClothesDto item,
    double score
) {

}
