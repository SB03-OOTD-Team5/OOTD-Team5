package com.sprint.ootd5team.domain.clothes.dto.response;

import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import java.util.UUID;

public record ClothesDto(
    UUID id,
    UUID ownerId,
    String name,
    String imageUrl,
    ClothesType type
//    ,List<ClothesAttributeWithDefDto> attributes
) {

}
