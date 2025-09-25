package com.sprint.ootd5team.domain.recommendation.dto;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import java.util.List;
import java.util.UUID;

public record RecommendationClothesDto(
    UUID clothesId,
    String name,
    String imageUrl,
    ClothesType type,
    List<ClothesAttributeWithDefDto> attributes
) {

    public RecommendationClothesDto {
        attributes = attributes == null ? List.of() : List.copyOf(attributes);
    }
}