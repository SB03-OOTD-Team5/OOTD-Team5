package com.sprint.ootd5team.domain.clothattribute.dto;

import java.util.List;

public record ClothesAttributeDefCreateRequest(
	String name,
	List<String> selectableValues
) {}
