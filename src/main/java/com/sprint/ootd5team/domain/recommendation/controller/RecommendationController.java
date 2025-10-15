package com.sprint.ootd5team.domain.recommendation.controller;

import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.domain.recommendation.controller.api.RecommendationApi;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationDto;
import com.sprint.ootd5team.domain.recommendation.service.RecommendationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController implements RecommendationApi {

    private final RecommendationService recommendationService;
    private final AuthService authService;

    @GetMapping
    @Override
    public ResponseEntity<RecommendationDto> getRecommendation(
        @RequestParam UUID weatherId,
        @RequestParam(required = false, defaultValue = "false") boolean useAi
    ) {
        log.info("[RecommendationController] 추천 조회 수신: weatherId={} ", weatherId);
        UUID userId = authService.getCurrentUserId();
        RecommendationDto recommendationDto = recommendationService.getRecommendation(weatherId,
            userId, useAi);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(recommendationDto);
    }

}
