package com.sprint.ootd5team.base.batch;

import com.sprint.ootd5team.base.batch.dto.WeatherBatchItem;
import com.sprint.ootd5team.domain.weather.service.WeatherFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherBatchWriter implements ItemWriter<WeatherBatchItem> {

    private final WeatherFactory weatherFactory;

    @Override
    public void write(List<? extends WeatherBatchItem> items) {
        for (WeatherBatchItem item : items) {
            try {
                weatherFactory.findOrCreateWeathers(item.latitude(), item.longitude(), item.profile());
                log.debug("[WeatherBatchWriter] 처리 완료: 프로필={} 위도={} 경도={}", item.profile().getId(), item.latitude(), item.longitude());
            } catch (Exception e) {
                log.error("[WeatherBatchWriter] 처리 실패: 프로필={} 원인={}", item.profile().getId(), e.getMessage(), e);
                throw e;
            }
        }
    }
}
