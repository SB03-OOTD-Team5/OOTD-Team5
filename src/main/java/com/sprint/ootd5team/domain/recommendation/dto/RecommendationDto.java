package com.sprint.ootd5team.domain.recommendation.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record RecommendationDto(
    UUID weatherId,
    UUID userId,
    List<RecommendationClothesDto> clothes
) {

}
