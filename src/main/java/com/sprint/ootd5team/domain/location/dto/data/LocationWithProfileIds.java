package com.sprint.ootd5team.domain.location.dto.data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record LocationWithProfileIds(
    UUID locationId,
    BigDecimal latitude,
    BigDecimal longitude,
    String locationNames,
    List<UUID> profileIds
) {

}
