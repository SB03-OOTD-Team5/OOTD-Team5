package com.sprint.ootd5team.domain.clothes.extractor;

import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;

public interface ClothesExtractionService {

    ClothesDto extractByUrl(String url);
}
