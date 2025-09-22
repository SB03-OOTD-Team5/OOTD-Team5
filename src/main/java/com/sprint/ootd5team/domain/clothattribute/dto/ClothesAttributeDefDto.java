package com.sprint.ootd5team.domain.clothattribute.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ClothesAttributeDefDto(
	UUID id,
	String name,
	List<String> selectableValues,
	Instant createdAt
) {}
