package com.sprint.ootd5team.domain.profile.dto.data;

import com.sprint.ootd5team.domain.location.dto.data.WeatherAPILocationDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ProfileUpdateRequest(
    @NotBlank
    @Size(max = 50)
    String name,
    String gender,
    LocalDate birthDate,
    WeatherAPILocationDto location,
    int temperatureSensitivity
) { }