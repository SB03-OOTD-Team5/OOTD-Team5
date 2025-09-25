package com.sprint.ootd5team.domain.weather.service;

import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.domain.location.dto.data.ClientCoords;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.mapper.WeatherMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final ProfileRepository profileRepository;
    private final WeatherFactory weatherFactory;
    private final WeatherMapper weatherMapper;

    @Override
    @Transactional
    public List<WeatherDto> fetchWeatherByLocation(BigDecimal latitude, BigDecimal longitude,
        UUID userId) {
        // 사용자 프로필 조회
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(ProfileNotFoundException::new);

        List<Weather> weathers = weatherFactory.findOrCreateWeathers(latitude, longitude, profile);

        //  최종 DTO로 변환하여 반환
        return weathers.stream()
            .map(weather -> weatherMapper.toDto(weather, new ClientCoords(latitude, longitude)))
            .toList();
    }
}