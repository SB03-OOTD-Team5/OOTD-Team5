package com.sprint.ootd5team.domain.recommendation.controller.api;

import com.sprint.ootd5team.domain.recommendation.dto.RecommendationDto;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface RecommendationApi {

    @GetMapping
    ResponseEntity<RecommendationDto> getRecommendation(@RequestParam UUID weatherId);
}
