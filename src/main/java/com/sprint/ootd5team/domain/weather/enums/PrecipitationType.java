package com.sprint.ootd5team.domain.weather.enums;

public enum PrecipitationType {
    NONE, // 0
    RAIN, // 1
    RAIN_SNOW, // 2
    SNOW, //3
    SHOWER; //4

    /** 비가 포함된 상태인지 */
    public boolean isRainy() {
        return this == RAIN || this == RAIN_SNOW || this == SHOWER;
    }

    /** 눈이 포함된 상태인지 */
    public boolean isSnowy() {
        return this == SNOW || this == RAIN_SNOW;
    }

    /** 강수(비/눈/소나기)가 전혀 없는지 */
    public boolean isClear() {
        return this == NONE;
    }
}
