package com.sprint.ootd5team.domain.weather.external.kma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KmaGridConverterTest {

    @Test
    @DisplayName("위경도를 격자로 변환하고 다시 경위도로 복원할 수 있다")
    void 위경도_격자_왕복이_가능하다() {
        double lon = 126.9780; // 서울 시청 근처
        double lat = 37.5665;

        KmaGridConverter.GridXY grid = KmaGridConverter.toGrid(lon, lat);
        KmaGridConverter.LonLatBD lonLat = KmaGridConverter.toLonLatBD(grid.x(), grid.y(), 4);

        assertEquals(Math.round(lon * 10_000d) / 10_000d,
            lonLat.lon().doubleValue(), 0.2);
        assertEquals(Math.round(lat * 10_000d) / 10_000d,
            lonLat.lat().doubleValue(), 0.2);
    }

    @Test
    @DisplayName("위경도 값이 유효하지 않으면 예외가 발생한다")
    void 위경도_범위가_잘못되면_예외가_발생한다() {
        assertThrows(IllegalArgumentException.class,
            () -> KmaGridConverter.toGrid(BigDecimal.valueOf(200), BigDecimal.ZERO));
    }
}
