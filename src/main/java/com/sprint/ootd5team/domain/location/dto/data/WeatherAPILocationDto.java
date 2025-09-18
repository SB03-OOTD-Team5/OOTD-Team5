package com.sprint.ootd5team.domain.location.dto.data;

import java.math.BigDecimal;

public record WeatherAPILocationDto(
    BigDecimal latitude, // 클라이언트에서 전달한 위도값
    BigDecimal longitude,// 클라이언트에서 전달한 경도값
    Integer x,
    Integer y,
    String[] locationNames,
    BigDecimal matchedLatitude, // 내부 확인용 - 서버에 저장된 위도값(소수점 4째 자리)
    BigDecimal matchedLongitude // 내부 확인용 - 서버에 저장된 경도값(소수점 4째 자리)
) {

}
