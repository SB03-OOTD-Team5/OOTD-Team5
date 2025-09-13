package com.sprint.ootd5team.domain.feed.dto.data;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDto;
import java.util.List;
import java.util.UUID;

public record OotdDto(
    UUID clothesId,
    String name,
    String imageUrl,
    String type,
    List<ClothesAttributeDto> attributes
) { }