package com.sprint.ootd5team.domain.recommendation.engine.model;


import com.sprint.ootd5team.domain.recommendation.dto.RecommendationClothesDto;
import java.util.List;

public record OutfitScore(
    List<RecommendationClothesDto> outfit,
    double score
) {

}