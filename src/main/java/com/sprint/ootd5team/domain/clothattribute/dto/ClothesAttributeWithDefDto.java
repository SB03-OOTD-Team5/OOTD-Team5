package com.sprint.ootd5team.domain.clothattribute.dto;

import com.sprint.ootd5team.domain.clothattribute.entity.ClothAttributeDefs;
import java.util.List;
import java.util.UUID;

public record ClothesAttributeWithDefDto(
	UUID definitionId,
	String definitionName,
	List<ClothAttributeDefs> selectableValues,
	String value
) {}
