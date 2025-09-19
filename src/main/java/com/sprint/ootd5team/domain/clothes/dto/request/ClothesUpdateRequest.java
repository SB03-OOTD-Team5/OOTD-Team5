package com.sprint.ootd5team.domain.clothes.dto.request;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDto;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

@Builder
public record ClothesUpdateRequest(

    @NotBlank
    String name,

    @NotNull
    ClothesType type,

    List<ClothesAttributeDto> attributes
) {

}
