package com.sprint.ootd5team.domain.recommendation.engine;

import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import com.sprint.ootd5team.domain.recommendation.dto.ApparentTemperatureDto;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.recommendation.enums.Season;
import com.sprint.ootd5team.domain.recommendation.mapper.RecommendationMapper;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
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

        // 허용 토큰(한글 displayName + aliases) 생성
        String[] allowedTokens = buildAllowedTokens(allowedSeasons);
        boolean includeAllSeason = true; // 사계절/기타 포함 여부

        // DB에서 계절 속성 매칭 의상만 조회
        List<UUID> ids = clothesRepository.findClothesIdsBySeasonFilter(userId, allowedTokens, includeAllSeason);

        List<Clothes> clothesList = ids.isEmpty()
            ? List.of()
            : clothesRepository.findAllWithAttributesByIds(ids);
        log.debug("[SeasonFilterEngine] [Filter] DB필터 결과 {}개", clothesList.size());

        return clothesList.stream()
            .map(recommendationMapper::toFilteredDto)
            .filter(dto -> {
                boolean include = dto.type() != ClothesType.ETC
                    || dto.optionalSubType() == null
                    || dto.optionalSubType().shouldInclude(info);

                if (dto.type() == ClothesType.ETC) {
                    log.debug("[SeasonFilterEngine] [ETC 필터] {} → subtype={} include={}",
                        dto.name(), dto.optionalSubType(), include);
                }
                return include;
            })
            .toList();
    }

    // 허용 가능 계절 목록 값
    private String[] buildAllowedTokens(EnumSet<Season> seasons) {
        return seasons.stream()
            .flatMap(s -> Stream.concat(
                Stream.of(s.getDisplayName()),
                Arrays.stream(s.getAliases())
            ))
            .filter(Objects::nonNull)
            .map(String::toLowerCase)
            .distinct()
            .toArray(String[]::new);
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

        log.debug("[SeasonFilterEngine] [Filter:ResolveSeason] forecast={}, feelsLike={}, sensitivity={}, 허용계절={}",
            forecast, String.format("%.1f", feelsLike), sensitivity, allowed);

        return allowed;
    }

}
