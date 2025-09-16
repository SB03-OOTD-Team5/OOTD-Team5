package com.sprint.ootd5team.base.errorcode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Security 관련 에러 코드
    UNAUTHORIZED("권한이 없는 사용자 입니다."),
    INVALID_USER_CREDENTIALS("잘못된 사용자 인증 정보입니다."),

    // User 관련 에러코드
    USER_NOT_FOUND("존재하지 않는 사용자 입니다."),
    USER_ALREADY_EXIST("이미 존재하는 사용자 입니다."),

    // Profile 관련 에러코드
    PROFILE_NOT_FOUND("존재하지 않는 프로필 입니다."),

    // Weather 관련 에러코드
    WEATHER_NOT_FOUND("해당하는 날씨 데이터가 없습니다."),
    WEATHER_KMA_FETCH_FAILED("기상청 데이터를 가져오는데 실패했습니다."),
    WEATHER_KMA_PARSE_FAILED("기상청 데이터를 분석하는데 실패했습니다.");


    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
