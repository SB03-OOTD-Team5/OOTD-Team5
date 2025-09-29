package com.sprint.ootd5team.domain.extract.dto;

import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import java.util.Map;

// LLM 추출
public record ClothesExtraInfo(
    String name,
    String typeRaw,
    Map<String, String> attributes
) {
    public ClothesType toClothesType() {
        if (typeRaw == null || typeRaw.isBlank()) {
            return ClothesType.ETC;
        }
        // "아우터 > 후드 집업" → "아우터"
        String mainType = typeRaw.split(">")[0].trim();
        return ClothesType.fromString(mainType);
    }
}

