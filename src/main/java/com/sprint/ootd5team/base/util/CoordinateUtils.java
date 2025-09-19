package com.sprint.ootd5team.base.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CoordinateUtils {

    // NUMERIC(8,4) → 소수점 이하 4자리, 반올림 적용
    public static BigDecimal toNumeric(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(4, RoundingMode.HALF_UP);
    }
}
