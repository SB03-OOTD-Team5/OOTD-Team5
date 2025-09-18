package com.sprint.ootd5team.domain.clothes.dto.response;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ClothesDto(
    UUID id,
    UUID ownerId,
    String name,
    String imageUrl,
    ClothesType type,
    List<ClothesAttributeWithDefDto> attributes,
    Instant createdAt,
    Instant updatedAt
) {

    public ClothesDto {
        attributes = attributes == null ? List.of() : List.copyOf(attributes);
    }
}
