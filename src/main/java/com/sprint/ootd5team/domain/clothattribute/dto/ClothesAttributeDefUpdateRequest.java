package com.sprint.ootd5team.domain.clothattribute.dto;

import java.util.List;

public record ClothesAttributeDefUpdateRequest(
	String name,
	List<String> selectableValues
) {}
