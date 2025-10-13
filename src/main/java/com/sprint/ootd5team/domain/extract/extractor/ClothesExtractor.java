package com.sprint.ootd5team.domain.extract.extractor;

import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;

public interface ClothesExtractor {

    ClothesDto extractByUrl(String url);
}
