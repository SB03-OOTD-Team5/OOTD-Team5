package com.sprint.ootd5team.domain.extract.dto;

import java.util.Map;

// LLM 추출
public record ClothesExtraInfo(
    String name,
    String typeRaw,
    Map<String, String> attributes
) {

}

