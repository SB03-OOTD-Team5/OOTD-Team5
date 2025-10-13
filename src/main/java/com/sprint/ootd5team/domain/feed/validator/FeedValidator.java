package com.sprint.ootd5team.domain.feed.validator;

import com.sprint.ootd5team.base.exception.clothes.ClothesNotFoundException;
import com.sprint.ootd5team.base.exception.feed.FeedNotFoundException;
import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import com.sprint.ootd5team.domain.feed.entity.Feed;
import com.sprint.ootd5team.domain.feed.repository.feed.FeedRepository;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.weather.exception.WeatherNotFoundException;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedValidator {

    private final FeedRepository feedRepository;
    private final ProfileRepository profileRepository;
    private final WeatherRepository weatherRepository;
    private final ClothesRepository clothesRepository;

    /**
     * feedId로 피드를 조회하고 없으면 예외를 던진다.
     */
    public Feed getFeedOrThrow(UUID feedId) {
        return feedRepository.findById(feedId)
            .orElseThrow(() -> {
                log.warn("[FeedValidator] 존재하지 않는 피드 - feedId:{}", feedId);
                return FeedNotFoundException.withId(feedId);
            });
    }

    /**
     * 작성자와 날씨 데이터가 유효한지 검증한다.
     */
    public void validateAuthorAndWeather(UUID authorId, UUID weatherId) {
        if (!profileRepository.existsByUserId(authorId)) {
            log.warn("[FeedValidator] 존재하지 않는 사용자 - userId:{}", authorId);
            throw ProfileNotFoundException.withUserId(authorId);
        }
        if (!weatherRepository.existsById(weatherId)) {
            log.warn("[FeedValidator] 존재하지 않는 날씨 - weatherId:{}", weatherId);
            throw new WeatherNotFoundException("존재하지 않는 날씨 데이터입니다.");
        }
    }

    /**
     * 의상 ID 목록이 유효한지 검증하고, 존재하는 Clothes 리스트를 반환한다.
     */
    public List<Clothes> validateClothes(Set<UUID> clothesIds) {
        List<Clothes> clothesList = clothesRepository.findAllById(clothesIds);

        Set<UUID> foundIds = clothesList.stream()
            .map(Clothes::getId)
            .collect(Collectors.toSet());

        Set<UUID> missingIds = new HashSet<>(clothesIds);
        missingIds.removeAll(foundIds);

        if (!missingIds.isEmpty()) {
            log.warn("[FeedValidator] 존재하지 않는 의상 - clothesIds:{}", missingIds);
            throw ClothesNotFoundException.withIds(missingIds);
        }

        return clothesList;
    }
}