package com.sprint.ootd5team.domain.weather.service;

import com.sprint.ootd5team.base.util.DateTimeUtils;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.time.Instant;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherCleanupService {

    private final WeatherRepository weatherRepository;

    /**
     * 오늘보다 이전 데이터 중, feed 쪽에서 사용하지 않은 weather 데이터는 삭제한다.
     *
     * @return 지운 컬럼 수
     */
    @Transactional
    public int deleteUnusedWeathersBeforeToday() {
        Instant todayStart = LocalDate.now(DateTimeUtils.SEOUL_ZONE_ID)
            .atStartOfDay(DateTimeUtils.SEOUL_ZONE_ID)
            .toInstant();
        int deleted = weatherRepository.deleteUnusedBefore(todayStart);
        log.info("[WeatherCleanupService] 컬럼: {}개 삭제, 기준 날짜: {} 이전", deleted, todayStart);
        return deleted;
    }
}
