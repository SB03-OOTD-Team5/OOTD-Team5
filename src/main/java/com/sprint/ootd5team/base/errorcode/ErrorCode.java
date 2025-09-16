package com.sprint.ootd5team.base.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Security 관련 에러 코드
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "권한이 없는 사용자 입니다."),
    INVALID_USER_CREDENTIALS(HttpStatus.UNAUTHORIZED, "잘못된 사용자 인증 정보입니다."),

    // User 관련 에러코드
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자 입니다."),
    USER_ALREADY_EXIST(HttpStatus.CONFLICT, "이미 존재하는 사용자 입니다."),

    // Feed 관련 에러 코드
    FEED_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 피드입니다."),
    INVALID_SORT_OPTION(HttpStatus.BAD_REQUEST, "유효하지 않은 정렬 옵션입니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
