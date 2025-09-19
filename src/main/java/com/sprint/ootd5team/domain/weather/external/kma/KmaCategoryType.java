package com.sprint.ootd5team.domain.weather.external.kma;

public enum KmaCategoryType {
    TMP, //temperature
    POP, //precipitationProbability
    PTY, //precipitationType
    SKY, //skyStatus
    WSD, //windspeed, windspeedLevel
    TMN, //temperatureMin
    TMX, //temperatureMax
    PCP, //precipitationAmount
    REH, //humidity
    UNKNOWN;
//    UUU,VVV,VEC,WAV;

    public static KmaCategoryType of(String type) {
        if (type == null) {
            return KmaCategoryType.UNKNOWN;
        }
        try {
            return KmaCategoryType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return KmaCategoryType.UNKNOWN;
        }
    }
}
