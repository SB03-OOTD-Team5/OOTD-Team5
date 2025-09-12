package com.sprint.ootd5team.domain.clothattribute.mapper;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefDto;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDto;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothAttribute;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothAttributeDef;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothAttributeValue;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClothesAttributeMapper {

	// ClothAttributeDef -> ClothesAttributeDto 변환
	@Mapping(source = "attribute.id", target = "definitionId")
	@Mapping(source = "value", target = "value")
	ClothesAttributeDto toDto(ClothAttributeDef clothAttributeDef);

	// ClothAttribute -> ClothesAttributeDefDto 변환
	@Mapping(source = "id", target = "id")
	@Mapping(source = "name", target = "name")
	@Mapping(source = "createdAt", target = "createdAt")
	@Mapping(target = "selectableValues", expression = "java(toValues(clothAttribute))")
	ClothesAttributeDefDto toDto(ClothAttribute clothAttribute);


	// ClothAttributeValue -> ClothesAttributeWithDefDto 변환
	@Mapping(source = "attribute.id", target = "definitionId")
	@Mapping(source = "attribute.name", target = "definitionName")
	@Mapping(source = "selectableValue", target = "value")
	@Mapping(target = "selectableValues", expression = "java(toSelectableValues(value.getAttribute()))")
	ClothesAttributeWithDefDto toDto(ClothAttributeValue value);

	// 속성 String 리스트로 추출,변환
	default List<String> extractValues(ClothAttribute attribute) {
		if (attribute.getDefs() == null) return List.of();
		return attribute.getDefs().stream()
			.map(ClothAttributeDef::getValue)
			.collect(Collectors.toList());
	}
}
