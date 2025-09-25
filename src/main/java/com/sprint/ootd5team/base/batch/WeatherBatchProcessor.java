package com.sprint.ootd5team.base.batch;

import com.sprint.ootd5team.base.batch.dto.WeatherBatchItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WeatherBatchProcessor implements ItemProcessor<WeatherBatchItem, WeatherBatchItem> {

    @Override
    public WeatherBatchItem process(WeatherBatchItem item) {
        log.debug("[WeatherBatchProcessor] 처리 대상 좌표: {}, {}", item.latitude(), item.longitude());
        return item;
    }
}
