package com.sprint.ootd5team.domain.recommendation.service;

import com.sprint.ootd5team.domain.recommendation.dto.RecommendationDto;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecommendationService {

    private final WeatherRepository weatherRepository;

    public RecommendationDto getRecommendation(UUID weatherId, UUID userId) {

        log.info("[RecommendationController] 추천 조회 시작: weatherId={}, userId={} ", weatherId,
            userId);

        // 해당 weather 정보 가져옴
        // 해당 weather와 비슷한 날씨 중, clothes가 있는 데이터 랜덤으로 1개 가져옴

        return null;

    }
}
