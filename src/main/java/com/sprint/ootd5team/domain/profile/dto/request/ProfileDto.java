package com.sprint.ootd5team.domain.profile.dto.request;

import com.sprint.ootd5team.domain.location.dto.data.WeatherAPILocationDto;
import java.time.LocalDate;
import java.util.UUID;

public record ProfileDto(
    UUID userId,
    String name,
    String gender,
    LocalDate birthDate,
    WeatherAPILocationDto location,
    int temperatureSensitivity,
    String profileImageUrl
) {

}