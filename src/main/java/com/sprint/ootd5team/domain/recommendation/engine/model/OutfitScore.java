package com.sprint.ootd5team.domain.recommendation.engine.model;


import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import java.util.List;

public record OutfitScore(
    List<ClothesFilteredDto> outfit,
    double score
) {

}