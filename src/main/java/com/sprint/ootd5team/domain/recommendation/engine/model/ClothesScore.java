package com.sprint.ootd5team.domain.recommendation.engine.model;

import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.enums.ClothesStyle;
import com.sprint.ootd5team.domain.recommendation.enums.ColorTone;
import com.sprint.ootd5team.domain.recommendation.enums.Material;

public record ClothesScore(
    ClothesFilteredDto item,
    double score,
    ColorTone tone,
    Material material,
    ClothesStyle style
) {

    public static ClothesScore from(ClothesFilteredDto dto, double score) {
        return new ClothesScore(
            dto,
            score,
            dto.colorTone(),
            dto.material(),
            dto.style()
        );
    }
}
