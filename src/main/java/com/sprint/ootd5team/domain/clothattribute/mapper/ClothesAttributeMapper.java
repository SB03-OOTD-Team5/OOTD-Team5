package com.sprint.ootd5team.domain.clothattribute.mapper;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefDto;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDto;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothAttribute;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothAttributeDefs;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClothesAttributeMapper {

	ClothesAttributeDto toDto(ClothAttribute clothAttribute);
	ClothesAttributeDefDto toDto(ClothAttributeDefs clothAttributeDefs);
}
