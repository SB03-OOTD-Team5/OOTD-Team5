package com.sprint.ootd5team.domain.clothesattribute.dto;

import java.util.UUID;

public record ClothesAttributeDto(
	UUID definitionId,
	String value
) {}
