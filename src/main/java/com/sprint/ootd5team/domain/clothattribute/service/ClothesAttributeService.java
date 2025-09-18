package com.sprint.ootd5team.domain.clothattribute.service;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefCreateRequest;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefDto;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefUpdateRequest;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDto;
import java.util.List;
import java.util.UUID;

public interface ClothesAttributeService {
	ClothesAttributeDefDto create(ClothesAttributeDefCreateRequest request);
	List<ClothesAttributeDefDto> findAll(String sortBy, String sortDirection, String keywordLike);
	ClothesAttributeDefDto update(UUID id, ClothesAttributeDefUpdateRequest request);
	List<ClothesAttributeDto> getValuesByAttributeId(UUID attributeId);
	void delete(UUID id);
}
