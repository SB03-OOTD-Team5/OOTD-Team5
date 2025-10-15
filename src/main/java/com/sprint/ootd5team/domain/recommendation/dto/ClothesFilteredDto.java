package com.sprint.ootd5team.domain.recommendation.dto;

import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothesattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.recommendation.enums.ClothesStyle;
import com.sprint.ootd5team.domain.recommendation.enums.Color;
import com.sprint.ootd5team.domain.recommendation.enums.ColorTone;
import com.sprint.ootd5team.domain.recommendation.enums.Material;
import com.sprint.ootd5team.domain.recommendation.enums.type.BottomType;
import com.sprint.ootd5team.domain.recommendation.enums.type.OuterType;
import com.sprint.ootd5team.domain.recommendation.enums.type.ShoesType;
import com.sprint.ootd5team.domain.recommendation.enums.type.TopType;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/**
 * 추천 필터링 이후의 의상 데이터
 * - attributes는 원본 속성
 * - colorTone/material/style/shoesType은 파싱된 enum 형태
 */
@Builder
public record ClothesFilteredDto(
    UUID clothesId,
    String name,
    String imageKey,
    ClothesType type,
    List<ClothesAttributeWithDefDto> attributes,

    // 파싱된 속성값
    Color color,
    ColorTone colorTone,
    Material material,
    ClothesStyle style,

    TopType topType,
    BottomType bottomType,
    OuterType outerType,
    ShoesType shoesType
) {
    /**
     * 테스트나 단순 생성용 - 상위 5개 필드만 받는 생성자
     */
    public ClothesFilteredDto(
        UUID clothesId,
        String name,
        String imageKey,
        ClothesType type,
        List<ClothesAttributeWithDefDto> attributes
    ) {
        this(
            clothesId,
            name,
            imageKey,
            type,
            attributes,
            null, null, null, null,
            null, null, null, null
        );
    }
}
