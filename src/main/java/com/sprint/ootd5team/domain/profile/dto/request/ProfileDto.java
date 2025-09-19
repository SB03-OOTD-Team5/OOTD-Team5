package com.sprint.ootd5team.domain.profile.dto.request;

import com.sprint.ootd5team.domain.location.entity.Location;
import java.time.LocalDate;
import java.util.UUID;

public record ProfileDto(
    UUID userId,
    String name,
    String gender,
    LocalDate birthDate,
    Location location,
    int temperatureSensitivity,
    String profileImageUrl
) { }