package com.sprint.ootd5team.domain.weather.dto.data;

import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;

public record WindSpeedDto(
    Double speed,
    WindspeedLevel asWord
) { }
