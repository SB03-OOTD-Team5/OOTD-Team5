package com.sprint.ootd5team.domain.clothattribute.dto;

import com.sprint.ootd5team.domain.clothattribute.entity.ClothAttributeDefs;
import java.util.List;

public record ClothesAttributeDefUpdateRequest(
	String name,
	List<ClothAttributeDefs> selectableValues
) {}
