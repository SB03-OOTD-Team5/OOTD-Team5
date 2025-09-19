package com.sprint.ootd5team.domain.weather.external.kma;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class KmaGridConverter {

    // 기상청 DFS 격자 크기(남한 한정)
    public static final int NX = 149;
    public static final int NY = 253;
    /**
     * 기상청 단기예보(DFS) 기본 파라미터
     */
    public static final LamcParameter DEFAULT = new LamcParameter(
        6371.00877, // Re
        5.0,        // grid
        30.0,       // slat1
        60.0,       // slat2
        126.0,      // olon
        38.0,       // olat
        210.0 / 5.0, // xo = 210 / grid
        675.0 / 5.0  // yo = 675 / grid
    );

    private KmaGridConverter() {
    }

    /**
     * 위경도 -> 격자(X,Y) (C 코드의 code=0 분기와 동일)
     */
    public static GridXY toGrid(double lon, double lat) {
        return toGrid(lon, lat, DEFAULT, true);
    }

    /**
     * 위경도 -> 격자(X,Y) (경계 체크 on/off 선택 가능)
     */
    public static GridXY toGrid(double lon, double lat, LamcParameter p, boolean clampToRange) {
        p.initIfNeeded();

        final double PI = p.PI;
        final double DEGRAD = p.DEGRAD;

        double ra = Math.tan(PI * 0.25 + lat * DEGRAD * 0.5);
        ra = p.re * p.sf / Math.pow(ra, p.sn);

        double theta = lon * DEGRAD - p.olonRad;
        if (theta > PI) {
            theta -= 2.0 * PI;
        }
        if (theta < -PI) {
            theta += 2.0 * PI;
        }
        theta *= p.sn;

        double x = ra * Math.sin(theta) + p.xo;
        double y = p.ro - ra * Math.cos(theta) + p.yo;

        // C 예제의 (int)(x1 + 1.5) 라운딩 규칙 동일 적용
        int gx = (int) (x + 1.5);
        int gy = (int) (y + 1.5);

        if (clampToRange) {
            if (gx < 1) {
                gx = 1;
            }
            if (gy < 1) {
                gy = 1;
            }
            if (gx > NX) {
                gx = NX;
            }
            if (gy > NY) {
                gy = NY;
            }
        }
        return new GridXY(gx, gy);
    }

    // ===== BigDecimal 지원: 위경도 -> 격자 =====

    /**
     * BigDecimal 위경도 -> 격자(X,Y)
     *
     * @param lon 경도(BigDecimal)
     * @param lat 위도(BigDecimal)
     */
    public static GridXY toGrid(BigDecimal lon, BigDecimal lat) {
        return toGrid(lon, lat, DEFAULT, true);
    }

    /**
     * BigDecimal 위경도 -> 격자(X,Y), 경계 체크 on/off
     */
    public static GridXY toGrid(BigDecimal lon, BigDecimal lat,
        LamcParameter p, boolean clampToRange) {
        if (lon == null || lat == null) {
            throw new IllegalArgumentException("lon/lat must not be null");
        }
        // 범위 간단 검증 (옵션)
        double dLon = lon.doubleValue();
        double dLat = lat.doubleValue();
        if (dLon < -180 || dLon > 180 || dLat < -90 || dLat > 90) {
            throw new IllegalArgumentException("Invalid lon/lat range");
        }
        return toGrid(dLon, dLat, p, clampToRange);
    }


    /**
     * 격자(X,Y) -> 위경도 (C 코드의 code=1 분기와 동일)
     */
    public static LonLat toLonLat(int x, int y) {
        return toLonLat(x, y, DEFAULT);
    }

    public static LonLat toLonLat(int x, int y, LamcParameter p) {
        p.initIfNeeded();

        final double PI = p.PI;
        final double RADDEG = p.RADDEG;

        // C 예제: x1 = x - 1; y1 = y - 1; 로 보정하여 lamcproj 호출
        double xn = (x - 1) - p.xo;
        double yn = p.ro - (y - 1) + p.yo;
        double ra = Math.hypot(xn, yn);

        if (p.sn < 0.0) {
            ra = -ra;
        }

        double alat = Math.pow((p.re * p.sf / ra), (1.0 / p.sn));
        alat = 2.0 * Math.atan(alat) - PI * 0.5;

        double theta;
        if (Math.abs(xn) <= 0.0) {
            theta = 0.0;
        } else if (Math.abs(yn) <= 0.0) {
            theta = PI * 0.5;
            if (xn < 0.0) {
                theta = -theta;
            }
        } else {
            theta = Math.atan2(xn, yn);
        }

        double alon = theta / p.sn + p.olonRad;

        double lat = alat * RADDEG;
        double lon = alon * RADDEG;
        return new LonLat(lon, lat);
    }

    // ===== BigDecimal 지원: 격자 -> 경위도 =====

    /**
     * 격자(X,Y) -> BigDecimal 경위도(기본 소수 6자리)
     */
    public static LonLatBD toLonLatBD(int x, int y) {
        return toLonLatBD(x, y, 6);
    }

    /**
     * 격자(X,Y) -> BigDecimal 경위도(임의 소수 자릿수)
     *
     * @param scale 소수 자릿수 (예: 6 → 소수점 6자리)
     */
    public static LonLatBD toLonLatBD(int x, int y, int scale) {
        var ll = toLonLat(x, y, DEFAULT); // double 계산
        // 필요한 경우 MathContext로 반올림 제어
        var mc = new MathContext(scale + 2, RoundingMode.HALF_UP);
        BigDecimal lon = new BigDecimal(ll.lon(), mc).setScale(scale, RoundingMode.HALF_UP);
        BigDecimal lat = new BigDecimal(ll.lat(), mc).setScale(scale, RoundingMode.HALF_UP);
        return new LonLatBD(lon, lat);
    }

    /**
     * 결과 DTO: 격자 좌표
     */
    public record GridXY(int x, int y) {

    }

    /**
     * 결과 DTO: 경위도
     */
    public record LonLat(double lon, double lat) {

    }

    /**
     * 결과 DTO: 경위도(BigDecimal)
     */
    public record LonLatBD(BigDecimal lon, BigDecimal lat) {

    }

    /**
     * LCC 투영 파라미터(C 원본과 동일)
     */
    public static final class LamcParameter {

        final double Re;    // 지구반경(km)
        final double grid;  // 격자 간격(km)
        final double slat1; // 표준위도1(deg)
        final double slat2; // 표준위도2(deg)
        final double olon;  // 기준경도(deg)
        final double olat;  // 기준위도(deg)
        final double xo;    // 기준 X(격자거리)
        final double yo;    // 기준 Y(격자거리)

        // 내부 계산 캐시
        private boolean initialized = false;
        private double PI, DEGRAD, RADDEG, re, olonRad, olatRad, sn, sf, ro;

        public LamcParameter(double Re, double grid, double slat1, double slat2,
            double olon, double olat, double xo, double yo) {
            this.Re = Re;
            this.grid = grid;
            this.slat1 = slat1;
            this.slat2 = slat2;
            this.olon = olon;
            this.olat = olat;
            this.xo = xo;
            this.yo = yo;
        }

        private void initIfNeeded() {
            if (initialized) {
                return;
            }
            PI = Math.asin(1.0) * 2.0;
            DEGRAD = PI / 180.0;
            RADDEG = 180.0 / PI;

            re = Re / grid;
            double slat1Rad = slat1 * DEGRAD;
            double slat2Rad = slat2 * DEGRAD;
            olonRad = olon * DEGRAD;
            olatRad = olat * DEGRAD;

            sn = Math.log(Math.cos(slat1Rad) / Math.cos(slat2Rad)) /
                Math.log(Math.tan(PI * 0.25 + slat2Rad * 0.5) /
                    Math.tan(PI * 0.25 + slat1Rad * 0.5));
            double sfTmp = Math.tan(PI * 0.25 + slat1Rad * 0.5);
            sf = Math.pow(sfTmp, sn) * Math.cos(slat1Rad) / sn;
            double roTmp = Math.tan(PI * 0.25 + olatRad * 0.5);
            ro = re * sf / Math.pow(roTmp, sn);

            initialized = true;
        }
    }
}
