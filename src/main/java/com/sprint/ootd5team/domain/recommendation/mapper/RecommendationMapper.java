package com.sprint.ootd5team.domain.recommendation.mapper;

import com.sprint.ootd5team.base.storage.FileStorage;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothesattribute.mapper.ClothesAttributeMapper;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationClothesDto;
import com.sprint.ootd5team.domain.recommendation.enums.ClothesStyle;
import com.sprint.ootd5team.domain.recommendation.enums.Color;
import com.sprint.ootd5team.domain.recommendation.enums.ColorTone;
import com.sprint.ootd5team.domain.recommendation.enums.Material;
import com.sprint.ootd5team.domain.recommendation.enums.type.BottomType;
import com.sprint.ootd5team.domain.recommendation.enums.type.OuterType;
import com.sprint.ootd5team.domain.recommendation.enums.type.ShoesType;
import com.sprint.ootd5team.domain.recommendation.enums.type.TopType;
import com.sprint.ootd5team.domain.recommendation.enums.util.EnumParser;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = ClothesAttributeMapper.class)
public abstract class RecommendationMapper {

    @Autowired
    protected FileStorage fileStorage;

    /** Clothes → RecommendationClothesDto (응답용) */
    @Mapping(source = "imageKey", target = "imageUrl", qualifiedByName = "resolveImageUrl")
    public abstract RecommendationClothesDto toDto(ClothesFilteredDto entity);

    /** Clothes → ClothesFilteredDto (필터링용) */
    @Mapping(source = "id", target = "clothesId")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "imageUrl", target = "imageKey")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "clothesAttributeValues", target = "attributes")
    @BeanMapping(ignoreByDefault = true)
    public abstract ClothesFilteredDto toFilteredDto(Clothes entity);

    @Named("resolveImageUrl")
    protected String resolveImageUrl(String path) {
        return fileStorage.resolveUrl(path);
    }

    @AfterMapping
    protected void parseEnums(
        @MappingTarget ClothesFilteredDto.ClothesFilteredDtoBuilder dtoBuilder,
        Clothes entity) {

        String name = entity.getName();

        String colorValue = getAttr(entity, "색상");
        String materialValue = getAttr(entity, "소재");
        String styleValue = getAttr(entity, "스타일");
        String shoesTypeValue = getAttr(entity, "신발타입");
        String topTypeValue = getAttr(entity, "상의 종류");
        String bottomTypeValue = getAttr(entity, "하의 종류");
        String outerTypeValue = getAttr(entity, "아우터 종류");

        // 색상 → 톤 자동 연계
        Color color = EnumParser.parseFromAttrAndName(Color.class, colorValue, name, Color.OTHER);
        ColorTone tone = color.tone();

        dtoBuilder
            .color(color)
            .colorTone(tone)
            .material(EnumParser.parseFromAttrAndName(
                Material.class, materialValue, name, Material.OTHER))
            .style(EnumParser.parseFromAttrAndName(
                ClothesStyle.class, styleValue, name, ClothesStyle.OTHER))
            .topType(entity.getType() == ClothesType.TOP
                ? EnumParser.parseFromAttrAndName(TopType.class, topTypeValue, name, TopType.OTHER)
                : TopType.OTHER)
            .bottomType(entity.getType() == ClothesType.BOTTOM
                ? EnumParser.parseFromAttrAndName(BottomType.class, bottomTypeValue, name, BottomType.OTHER)
                : BottomType.OTHER)
            .outerType(entity.getType() == ClothesType.OUTER
                ? EnumParser.parseFromAttrAndName(OuterType.class, outerTypeValue, name, OuterType.OTHER)
                : OuterType.OTHER)
            .shoesType(entity.getType() == ClothesType.SHOES
                ? EnumParser.parseFromAttrAndName(ShoesType.class, shoesTypeValue, name, ShoesType.OTHER)
                : ShoesType.OTHER);
    }

    // === 의상 속성값 찾기 헬퍼 ===
    private String getAttr(Clothes entity, String key) {
        return entity.getClothesAttributeValues().stream()
            .filter(attr -> attr.getAttribute().getName().equalsIgnoreCase(key))
            .findFirst()
            .map(attr -> attr.getDefValue())
            .orElse("");
    }
}