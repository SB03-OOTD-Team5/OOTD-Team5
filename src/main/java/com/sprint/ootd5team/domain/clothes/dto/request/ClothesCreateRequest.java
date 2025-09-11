package com.sprint.ootd5team.domain.clothes.dto.request;

import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ClothesCreateRequest(

    @NotNull
    UUID ownerId,

    @NotBlank
    String name,

    @NotNull
    ClothesType type

//    , List<ClothesAttributeWithDefDto> attributes
) {

}
