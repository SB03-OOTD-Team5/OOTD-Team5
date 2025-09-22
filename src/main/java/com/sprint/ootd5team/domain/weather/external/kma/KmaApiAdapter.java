package com.sprint.ootd5team.domain.weather.external.kma;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.domain.weather.exception.ConvertCoordFailException;
import com.sprint.ootd5team.domain.weather.exception.WeatherKmaFetchException;
import com.sprint.ootd5team.domain.weather.exception.WeatherKmaParseException;
import com.sprint.ootd5team.domain.weather.external.kma.KmaGridConverter.GridXY;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class KmaApiAdapter {

    private final WebClient KmaWebClient;
    private final ObjectMapper mapper;
    private final String baseTime = "0200";

    public KmaApiAdapter(@Qualifier("KmaWebClient") WebClient KmaWebClient,
        ObjectMapper mapper) {
        this.KmaWebClient = KmaWebClient;
        this.mapper = mapper;
    }

    public KmaResponseDto fetchWeatherFromKma(String baseDate, BigDecimal latitude,
        BigDecimal longitude) {
        String responseJson = fetchRawWeatherData(baseDate, latitude, longitude);
        KmaResponseDto kmaResponseDto = parseToKmaResponseDto(responseJson);
        validateKmaData(kmaResponseDto);
        return kmaResponseDto;
    }

    private String fetchRawWeatherData(String baseDate, BigDecimal latitude, BigDecimal longitude) {
        GridXY kmaXY = convertGridXY(latitude, longitude);

        try {
            log.debug(
                "[Weather] 날씨 정보 조회 요청 longitude:{},latitude:{},x:{},y:{},base date:{},base time:{}",
                longitude, latitude,
                kmaXY.x(), kmaXY.y(), baseDate, baseTime);

            String response = KmaWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 1000)
                    .queryParam("base_date", baseDate)
                    .queryParam("base_time", baseTime)
                    .queryParam("nx", kmaXY.x())
                    .queryParam("ny", kmaXY.y())
                    .build())
                .exchangeToMono(
                    clientResponse -> clientResponse.bodyToMono(String.class)
                )
                .block();
            log.debug("[Weather] 날씨 정보 Fetch 완료");

            return response;
        } catch (Exception e) {
            throw new WeatherKmaFetchException();
        }
    }

    private GridXY convertGridXY(BigDecimal latitude, BigDecimal longitude) {
        try {
            GridXY gridXY = KmaGridConverter.toGrid(longitude, latitude);
            log.debug("[Weather] 좌표변환완료: x:{},y:{}", gridXY.x(), gridXY.y());
            return gridXY;
        } catch (Exception e) {
            ConvertCoordFailException ex = new ConvertCoordFailException();
            ex.addDetail("latitude", latitude);
            ex.addDetail("longitude", longitude);
            throw ex;
        }
    }

    private KmaResponseDto parseToKmaResponseDto(String responseJson) {
        try {
            return mapper.readValue(responseJson, KmaResponseDto.class);
        } catch (Exception e) {
            throw new WeatherKmaParseException(e.getMessage());
        }
    }

    private void validateKmaData(KmaResponseDto kmaDto) {
        if (!kmaDto.response().header().resultCode().equals("00")) {
            throw new WeatherKmaParseException(kmaDto.response().header().resultMsg());
        }
    }
}
