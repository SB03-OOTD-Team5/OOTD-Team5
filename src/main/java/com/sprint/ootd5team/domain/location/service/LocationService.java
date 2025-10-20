package com.sprint.ootd5team.domain.location.service;

import com.sprint.ootd5team.domain.location.dto.data.LocationWithProfileIds;
import com.sprint.ootd5team.domain.location.dto.data.WeatherAPILocationDto;
import com.sprint.ootd5team.domain.location.entity.Location;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public interface LocationService {

    @Transactional
    WeatherAPILocationDto fetchLocation(BigDecimal latitude, BigDecimal longitude, UUID userId);

    Location findOrCreateLocation(BigDecimal latitude, BigDecimal longitude);

    List<LocationWithProfileIds> findAllLocationUsingInProfileDistinct();
}
