package com.sprint.ootd5team.domain.clothattribute.service;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefCreateRequest;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothAttribute;
import java.util.List;

public interface ClothAttributeService {
	ClothAttribute create(ClothesAttributeDefCreateRequest request);
	List<ClothAttribute> getClothAttributes();
	ClothAttribute getClothAttributeById(Long id);
}
