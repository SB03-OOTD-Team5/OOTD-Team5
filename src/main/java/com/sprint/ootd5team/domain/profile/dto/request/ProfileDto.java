package com.sprint.ootd5team.domain.profile.dto.request;

import java.time.LocalDate;
import java.util.UUID;

public record ProfileDto(
    UUID userId,
    String name,
    String gender,
    LocalDate birthDate,
//    LocationDto location,
    int temperatureSensitivity,
    String profileImageUrl
) { }