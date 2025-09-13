package com.sprint.ootd5team.domain.profile.dto.data;

import java.time.LocalDate;

public record ProfileUpdateRequest(
    String name,
    String gender,
    LocalDate birthDate,
//    LocationDto location,
    int temperatureSensitivity
) { }