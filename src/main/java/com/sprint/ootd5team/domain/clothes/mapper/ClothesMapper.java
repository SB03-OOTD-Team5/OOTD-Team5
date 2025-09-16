package com.sprint.ootd5team.domain.clothes.mapper;

import com.sprint.ootd5team.domain.clothattribute.mapper.ClothesAttributeMapper;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ClothesAttributeMapper.class)
public interface ClothesMapper {

    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "clothesAttributeValues", target = "attributes")
    ClothesDto toDto(Clothes entity);

}
