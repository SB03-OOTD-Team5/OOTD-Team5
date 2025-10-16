package com.sprint.ootd5team.domain.recommendation.engine;

import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttributeValue;
import com.sprint.ootd5team.domain.recommendation.dto.ApparentTemperatureDto;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.recommendation.enums.Season;
import com.sprint.ootd5team.domain.recommendation.enums.util.EnumParser;
import com.sprint.ootd5team.domain.recommendation.mapper.RecommendationMapper;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
@Slf4j
public class SeasonFilterEngine {
    private final ClothesRepository clothesRepository;
    private final RecommendationMapper recommendationMapper;

    /**
     * 사용자/날씨 기반으로 의상을 사전 필터링
     * - 현재는 '계절' 속성 기준으로 필터링
     */
    @Transactional(readOnly = true)
    public List<ClothesFilteredDto> getFilteredClothes(UUID userId, RecommendationInfoDto info) {
        // 민감도 기반 허용 계절 계산
        EnumSet<Season> allowedSeasons = resolveAllowedSeasons(info);
        log.debug("[Filter] 허용 계절 EnumSet = {}", allowedSeasons);

        // 계절 속성 있는 의상
        List<Clothes> clothesList = clothesRepository.findByOwnerWithSeasonAttribute(userId);
        log.debug("[Filter] 계절 속성 보유 의상 {}개", clothesList.size());

        // 3) 자바단 필터 + 매핑
        List<ClothesFilteredDto> filtered = clothesList.stream()
            .filter(c -> matchesAllowedSeason(c, allowedSeasons))
            .map(recommendationMapper::toFilteredDto)
            .toList();

        log.debug("[Filter] 최종 필터링 결과 {}개 (전체 {}개 중)", filtered.size(), clothesList.size());
        return filtered;
    }

    /** 의상의 계절 속성값과 허용 계절 비교 */
    private boolean matchesAllowedSeason(Clothes clothes, EnumSet<Season> allowedSeasons) {
        String seasonValue = clothes.getClothesAttributeValues().stream()
            .filter(v -> "계절".equals(v.getAttribute().getName()))
            .map(ClothesAttributeValue::getDefValue)
            .findFirst()
            .orElse("");

        if (seasonValue.isBlank()) {
            log.trace("[Filter:Skip] '{}' → 계절 속성 없음", clothes.getName());
            return false;
        }

        // 복합값 처리: 봄/가을 같은 경우
        List<Season> parsedSeasons = Arrays.stream(seasonValue.split("/"))
            .map(part -> EnumParser.safeParse(Season.class, part.trim(), Season.OTHER))
            .toList();

        boolean match = parsedSeasons.stream()
            .anyMatch(s -> s == Season.OTHER || allowedSeasons.contains(s));

        log.debug(
            "[Filter:Check] '{}' 의상 계절={}, 허용 계절={}, 매칭결과={}",
            clothes.getName(), parsedSeasons, allowedSeasons, match
        );

        return match;
    }

    /** 사용자 민감도 기반으로 허용 계절 EnumSet 계산 */
    private EnumSet<Season> resolveAllowedSeasons(RecommendationInfoDto info) {
        WeatherInfoDto weatherInfo = info.weatherInfo();
        ApparentTemperatureDto temp = weatherInfo.apparentTemperature();

        // 예보 시점의 실제 계절
        Season forecast = Season.from(temp.forecastAt());

        double feelsLike = info.personalFeelsTemp();
        int sensitivity = info.profileInfo().temperatureSensitivity();

        EnumSet<Season> allowed = EnumSet.of(forecast);

        if (sensitivity == 1) {
            switch (forecast) {
                case WINTER -> {}
                case AUTUMN, SPRING -> allowed.add(Season.WINTER);
                case SUMMER -> allowed.addAll(EnumSet.of(Season.SPRING, Season.AUTUMN));
            }
        } else if (sensitivity == 5) {
            switch (forecast) {
                case SUMMER -> {}
                case AUTUMN, SPRING -> allowed.add(Season.SUMMER);
                case WINTER -> allowed.addAll(EnumSet.of(Season.SPRING, Season.AUTUMN));
            }
        } else {
            switch (forecast) {
                case SPRING -> allowed.add(Season.SUMMER);
                case SUMMER -> allowed.add(Season.SPRING);
                case AUTUMN -> allowed.add(Season.WINTER);
                case WINTER -> allowed.add(Season.AUTUMN);
            }
        }

        log.debug("[Filter:ResolveSeason] forecast={}, feelsLike={}, sensitivity={}, 허용계절={}",
            forecast, String.format("%.1f", feelsLike), sensitivity, allowed);

        return allowed;
    }

}
