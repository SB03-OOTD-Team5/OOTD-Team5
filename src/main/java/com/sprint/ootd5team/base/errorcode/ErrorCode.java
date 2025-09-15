package com.sprint.ootd5team.base.errorcode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Security 관련 에러 코드
    UNAUTHORIZED("권한이 없는 사용자 입니다."),
    INVALID_USER_CREDENTIALS("잘못된 사용자 인증 정보입니다."),

    // User 관련 에러코드
    USER_NOT_FOUND("존재하지 않는 사용자 입니다."),
    USER_ALREADY_EXIST("이미 존재하는 사용자 입니다.");




    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
