package com.sprint.ootd5team.domain.clothesattribute.mapper;

import com.sprint.ootd5team.domain.clothesattribute.dto.ClothesAttributeDefDto;
import com.sprint.ootd5team.domain.clothesattribute.dto.ClothesAttributeDto;
import com.sprint.ootd5team.domain.clothesattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttributeDef;
import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttributeValue;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ClothesAttributeMapper {

	// 1) 허용값 정의 -> 단일 값 DTO
	@Mapping(source = "attribute.id", target = "definitionId") // 부모 카테고리 ID
	@Mapping(source = "attDef",        target = "value")        // att_def 문자열
	ClothesAttributeDto toDto(ClothesAttributeDef def);

	// 2) 카테고리 -> 카테고리+허용값 목록 DTO
	@Mapping(source = "id",        target = "id")
	@Mapping(source = "name",      target = "name")
	@Mapping(source = "createdAt", target = "createdAt")
	@Mapping(target = "selectableValues", source = "defs", qualifiedByName = "defsToStrings")
	ClothesAttributeDefDto toDto(ClothesAttribute attr);

	// 3) 실제 값 -> 카테고리/허용값 목록/선택값 DTO
	@Mapping(source = "attribute.id",   target = "definitionId")
	@Mapping(source = "attribute.name", target = "definitionName")
	@Mapping(source = "defValue", target = "value")
	@Mapping(target = "selectableValues", source = "attribute.defs", qualifiedByName = "defsToStrings")
	ClothesAttributeWithDefDto toDto(ClothesAttributeValue val);

	// ===== 공용 헬퍼=====
	@Named("defsToStrings")
	default List<String> defsToStrings(List<ClothesAttributeDef> defs) {
		if (defs == null) return List.of();
		return defs.stream().map(ClothesAttributeDef::getAttDef).collect(Collectors.toList());
	}
}
