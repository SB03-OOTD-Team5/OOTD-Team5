package com.sprint.ootd5team.domain.recommendation.engine.model;

import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.enums.ClothesStyle;
import com.sprint.ootd5team.domain.recommendation.enums.Color;
import com.sprint.ootd5team.domain.recommendation.enums.ColorTone;
import com.sprint.ootd5team.domain.recommendation.enums.Material;

public record ClothesScore(
    ClothesFilteredDto item,
    double score,
    ColorTone tone,
    Material material,
    ClothesStyle style
) {

    /** DTO에서 주요 속성 추출하여 ScoredClothes로 변환 */
    public static ClothesScore from(ClothesFilteredDto dto, double score) {
        String color = getAttr(dto, "색상");
        String material = getAttr(dto, "소재");
        String style = getAttr(dto, "스타일");

        return new ClothesScore(
            dto,
            score,
            Color.fromString(color).tone(),
            Material.fromString(material),
            ClothesStyle.fromString(style)
        );
    }

    private static String getAttr(ClothesFilteredDto clothes, String key) {
        return clothes.attributes().stream()
            .filter(a -> a.definitionName().equals(key))
            .map(a -> a.value())
            .findFirst()
            .orElse("");
    }
}
