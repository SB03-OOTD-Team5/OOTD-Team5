package com.sprint.ootd5team.base.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class DateTimeUtils {

    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(
        "yyyyMMddHHmm");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");
    public static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");


    public static Instant toInstant(String date, String time) {
        LocalDateTime ldt = LocalDateTime.parse(date + time, DATETIME_FORMATTER);
        return ldt.atZone(SEOUL_ZONE_ID).toInstant();
    }

    public static Instant toInstant(LocalDate localDate, LocalTime localTime) {
        Objects.requireNonNull(localDate, "issueDate must not be null");
        Objects.requireNonNull(localDate, "issueTime must not be null");
        return localDate.atTime(localTime).atZone(SEOUL_ZONE_ID).toInstant();
    }

    public static ZonedDateTime getZonedDateTime(LocalDate baseDate, LocalTime baseTime) {
        ZonedDateTime result = ZonedDateTime.of(baseDate, baseTime, SEOUL_ZONE_ID);
        log.debug("[OpenWeather] zone 포함 일시 생성 baseDate:{}, baseTime:{}, result:{}", baseDate,
            baseTime, result);
        return result;
    }

}
