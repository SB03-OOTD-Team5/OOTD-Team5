package com.sprint.ootd5team.domain.clothes.mapper;

import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClothesMapper {

    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(target = "attributes", expression = "java(java.util.List.of())")
    ClothesDto toDto(Clothes entity);

}
