package com.sprint.ootd5team.domain.weather.external.kma;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 기상청 단기예보(예시) 외부 API 응답 record
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public record KmaResponse(Response response) {

    public static record Response(
        @JsonProperty("header") Header header,
        @JsonProperty("body") Body body
    ) {

    }

    public static record Header(
        /*
         *  "resultCode": "00", "resultMsg": "NORMAL_SERVICE"
         *  "resultCode": "10", "resultMsg": "최근 3일 간의 자료만 제공합니다."
         * "resultCode": "03",   "resultMsg": "NO_DATA"
         * */

        @JsonProperty("resultCode") String resultCode,
        @JsonProperty("resultMsg") String resultMsg
    ) {

    }

    public static record Body(
        @JsonProperty("dataType") String dataType,
        @JsonProperty("items") Items items,
        @JsonProperty("pageNo") Integer pageNo,
        @JsonProperty("numOfRows") Integer numOfRows,
        @JsonProperty("totalCount") Integer totalCount
    ) {

    }

    public static record Items(
        @JsonProperty("item") List<WeatherItem> weatherItems
    ) {

    }

    public static record WeatherItem(
        @JsonProperty("baseDate")
        String baseDate,   // "yyyyMMdd"
        @JsonProperty("baseTime")
        String baseTime,   // "HHmm"

        // TMN(일 최저기온),TMX(일 최고기온) 안넘어옴
        /**
         *  ex)  POP(강수확률),PTY(강수형태),PCP(1시간 강수량),REH(습도),SNO(1시간 신적설),
         * SKY(하늘상태),TMP(1시간 기온),TMN(일 최저기온),TMX(일 최고기온),
         * UUU(풍속(동서성분)),VVV(풍속(남북성분)),WAV(파고),VEC(풍향),WSD(풍속)
         */
        @JsonProperty("category")
        String category,
        @JsonProperty("fcstDate")
        String fcstDate,   // "yyyyMMdd"
        @JsonProperty("fcstTime")
        String fcstTime,   // "HHmm"
        @JsonProperty("fcstValue")
        String fcstValue, // 숫자/문자 혼재("강수없음") → 문자열로 받는게 안전
        @JsonProperty("nx")
        Integer nx,
        @JsonProperty("ny")
        Integer ny
    ) {

    }
}