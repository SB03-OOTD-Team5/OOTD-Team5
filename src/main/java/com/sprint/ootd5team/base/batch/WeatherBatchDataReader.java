package com.sprint.ootd5team.base.batch;

import com.sprint.ootd5team.domain.location.dto.data.LocationWithProfileIds;
import com.sprint.ootd5team.domain.location.service.LocationService;
import com.sprint.ootd5team.domain.weather.service.WeatherService;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.stereotype.Component;

/**
 * @class WeatherBatchDataReader
 * @brief 위치별로 프로필 ID 묶음을 제공해 배치가 처리할 LocationWithProfileIds를 공급한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherBatchDataReader implements ItemStreamReader<LocationWithProfileIds>,
    ItemStream {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");
    private final LocationService locationService;
    private final WeatherService weatherService;
    //    private final String BASE_TIME = "2300";
    private final String BASE_TIME = "0800"; // 테스트용
    private Iterator<LocationWithProfileIds> iterator;

    /**
     * @return 다음 LocationWithProfileIds 또는 더 이상 없을 때 null
     * @brief 준비된 Iterator에서 다음 배치 아이템을 반환한다.
     */
    @Override
    public LocationWithProfileIds read() {
        if (iterator == null || !iterator.hasNext()) {
            log.info("[WeatherBatchDataReader] 더 이상 처리할 Location 정보가 없습니다.");
            return null;
        }
        LocationWithProfileIds withProfileIds = iterator.next();

        log.info("[WeatherBatchDataReader] 위도:{}, 경도:{}, 사용자 Id:{}",
            withProfileIds.latitude(), withProfileIds.longitude(),
            withProfileIds.profileIds().toString());

        return withProfileIds;
    }

    /**
     * @brief Reader 시작 시 위치가 설정된 프로필만 필터링해 Iterator를 초기화한다.
     */
    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        List<LocationWithProfileIds> locationWithProfileIds = locationService.findAllLocationUsingInProfileDistinct();

        iterator = locationWithProfileIds.iterator();
        log.info("[WeatherBatchDataReader] 총 {}건의 Location 묶음을 로드",
            locationWithProfileIds.size());
    }

    @Override
    public void close() throws ItemStreamException {
        iterator = null;
    }
}
