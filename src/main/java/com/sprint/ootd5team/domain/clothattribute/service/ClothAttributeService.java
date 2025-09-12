package com.sprint.ootd5team.domain.clothattribute.service;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefCreateRequest;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothAttribute;
import java.util.List;
import java.util.UUID;

public interface ClothAttributeService {
	ClothAttribute create(ClothesAttributeDefCreateRequest request);
	List<ClothAttribute> findAll();
	ClothAttribute find(UUID id);
}
