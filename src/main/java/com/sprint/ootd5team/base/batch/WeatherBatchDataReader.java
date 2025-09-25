package com.sprint.ootd5team.base.batch;

import com.sprint.ootd5team.base.batch.dto.WeatherBatchItem;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherBatchDataReader implements ItemStreamReader<WeatherBatchItem>, ItemStream {

    private final ProfileRepository profileRepository;

    private Iterator<Profile> profileIterator;

    @Override
    public WeatherBatchItem read() {
        if (profileIterator == null) {
            return null;
        }
        if (!profileIterator.hasNext()) {
            return null;
        }
        Profile profile = profileIterator.next();
        BigDecimal latitude = profile.getLatitude();
        BigDecimal longitude = profile.getLongitude();
        if (latitude == null || longitude == null) {
            log.debug("[WeatherBatchDataReader] 좌표가 없는 프로필 건너뜀: {}", profile.getId());
            return read();
        }
        return new WeatherBatchItem(latitude, longitude, profile);
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        List<Profile> profiles = profileRepository.findAll();
        profileIterator = profiles.iterator();
        log.info("[WeatherBatchDataReader] 총 {}건의 프로필 로드", profiles.size());
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
    }

    @Override
    public void close() throws ItemStreamException {
        profileIterator = null;
    }
}
