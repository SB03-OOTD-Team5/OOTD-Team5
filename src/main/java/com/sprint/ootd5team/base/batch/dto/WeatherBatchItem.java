package com.sprint.ootd5team.base.batch.dto;

import com.sprint.ootd5team.domain.profile.entity.Profile;
import java.math.BigDecimal;

public record WeatherBatchItem(BigDecimal latitude, BigDecimal longitude, Profile profile) {
}
