package com.sprint.ootd5team.domain.clothesattribute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ClothesAttributeDefCreateRequest(
	@NotBlank @Size(max = 50) String name,
	@NotEmpty List<@NotBlank @Size(max = 50) String> selectableValues
) {}
