package com.sprint.ootd5team.domain.clothattribute.dto;

import com.sprint.ootd5team.domain.clothattribute.entity.ClothAttributeDefs;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ClothesAttributeDefDto(
	UUID id,
	String name,
	List<ClothAttributeDefs> selectabelValues,
	Instant createdAt
) {}
