package com.sprint.ootd5team.domain.clothattribute.dto;

import java.util.UUID;

public record ClothesAttributeDto(
	UUID definitionId,
	String value
) {}
