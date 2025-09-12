package com.sprint.ootd5team.domain.clothattribute.service;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothAttributeValue;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothesAttributeValueService {
	ClothesAttributeWithDefDto create(UUID clothesId, UUID attributeId, String value);
	List<ClothesAttributeWithDefDto> getByClothesId(UUID clothesId);

}

