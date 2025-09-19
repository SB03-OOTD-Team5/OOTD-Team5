package com.sprint.ootd5team.domain.weather.external.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

/**
 * 카카오 좌표 → 행정구역 변환(coord2regioncode) 외부 API 응답 record
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoResponseDto(
    @JsonProperty("meta")
    Meta meta,
    @JsonProperty("documents")
    List<Document> documents
) {

    public static record Meta(
        @JsonProperty("total_count") Integer totalCount
    ) {

    }

    public static record Document(
        @JsonProperty("region_type")
        String regionType,                    // ex) "B":법정동, "H": 행정동
        @JsonProperty("code")
        String code,
        // ex) "1150010100"
        @JsonProperty("address_name")
        String addressName,                    // 전체 행정구역명
        @JsonProperty("region_1depth_name")
        String region1depthName,                // 시/도
        @JsonProperty("region_2depth_name")
        String region2depthName,                // 시/군/구
        @JsonProperty("region_3depth_name")
        String region3depthName,                // 읍/면/동
        @JsonProperty("region_4depth_name")
        String region4depthName,                // 리 (없을 수 있음)
        @JsonProperty("x")
        BigDecimal x,                           // 괸측소 경도
        @JsonProperty("y")
        BigDecimal y                            // 괸측소 위도
    ) {

    }
}
