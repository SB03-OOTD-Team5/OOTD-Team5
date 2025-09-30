package com.sprint.ootd5team.domain.clothesattribute.dto;

import java.util.List;
import java.util.UUID;

public record ClothesAttributeWithDefDto(
	UUID definitionId,
	String definitionName,
	List<String> selectableValues,
	String value
) {}
