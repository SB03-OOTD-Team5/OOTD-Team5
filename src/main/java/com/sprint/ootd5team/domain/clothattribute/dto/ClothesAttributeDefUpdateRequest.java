package com.sprint.ootd5team.domain.clothattribute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ClothesAttributeDefUpdateRequest(
    @NotBlank @Size(max = 50) String name,
    @Size(max = 50) List<@NotBlank @Size(max = 50) String> selectableValues
) {}
