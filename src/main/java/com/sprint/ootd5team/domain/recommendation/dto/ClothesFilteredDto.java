package com.sprint.ootd5team.domain.recommendation.dto;

import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothesattribute.dto.ClothesAttributeWithDefDto;
import java.util.List;
import java.util.UUID;

public record ClothesFilteredDto(
    UUID clothesId,
    String name,
    String imageKey,
    ClothesType type,
    List<ClothesAttributeWithDefDto> attributes
) {

}
