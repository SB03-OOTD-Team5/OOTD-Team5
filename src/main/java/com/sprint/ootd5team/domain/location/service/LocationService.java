package com.sprint.ootd5team.domain.location.service;

import com.sprint.ootd5team.domain.location.dto.data.WeatherAPILocationDto;
import java.math.BigDecimal;

public interface LocationService {

    WeatherAPILocationDto fetchLocation(BigDecimal longitude, BigDecimal latitude);
}
