package com.sprint.ootd5team.domain.location.service;

import java.math.BigDecimal;

public interface LocationQueryService {

    /**
     * 위/경도를 입력하면 행정동 문자열을 반환(캐시/DB 조회 후 없으면 외부API 조회)
     */
    String getLocationNames(BigDecimal latitude, BigDecimal longitude);

}
