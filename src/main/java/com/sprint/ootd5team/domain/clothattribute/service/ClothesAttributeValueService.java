package com.sprint.ootd5team.domain.clothattribute.service;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeWithDefDto;
import java.util.List;
import java.util.UUID;

public interface ClothesAttributeValueService {
	ClothesAttributeWithDefDto create(UUID clothesId, UUID attributeId, String value);
	List<ClothesAttributeWithDefDto> getByClothesId(UUID clothesId);

}

