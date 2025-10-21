package com.sprint.ootd5team.domain.recommendation.engine.model;

import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.enums.ClothesStyle;
import com.sprint.ootd5team.domain.recommendation.enums.Color;
import com.sprint.ootd5team.domain.recommendation.enums.ColorTone;
import com.sprint.ootd5team.domain.recommendation.enums.Material;
import com.sprint.ootd5team.domain.recommendation.enums.type.BottomType;
import com.sprint.ootd5team.domain.recommendation.enums.type.OuterType;
import com.sprint.ootd5team.domain.recommendation.enums.type.ShoesType;
import com.sprint.ootd5team.domain.recommendation.enums.type.TopType;

public record ClothesScore(
    ClothesFilteredDto item,
    double score
) {

    public static ClothesScore from(ClothesFilteredDto dto, double score) {
        return new ClothesScore(dto, score);
    }

    public ColorTone tone() { return item.colorTone(); }
    public Material material() { return item.material(); }
    public ClothesStyle style() { return item.style(); }
    public Color color() { return item.color(); }

    public ClothesType type() { return item.type(); }
    public TopType topType() { return item.topType(); }
    public BottomType bottomType() { return item.bottomType(); }
    public OuterType outerType() { return item.outerType(); }
    public ShoesType shoesType() { return item.shoesType(); }

    public String name() { return item.name(); }

}
