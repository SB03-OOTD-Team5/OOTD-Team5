package com.sprint.ootd5team.base.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Security 관련 에러 코드
    UNAUTHORIZED("권한이 없는 사용자 입니다."),
    INVALID_USER_CREDENTIALS("잘못된 사용자 인증 정보입니다."),

    // User 관련 에러코드
    USER_NOT_FOUND("존재하지 않는 사용자 입니다."),
    USER_ALREADY_EXIST("이미 존재하는 사용자 입니다."),

    // Clothes 관련 에러코드
    CLOTHES_NOT_FOUND("존재하지 않는 의상 입니다."),

    // File 관련 에러코드
    FILE_SAVE_FAILED("파일 업로드 실패 - 재시도 가능"),
    FILE_PERMANENT_SAVE_FAILED("파일 업로드 실패 - 모든 재시도 소진"),
    FILE_DELETE_FAILED("파일 삭제 실패"),
    FILE_TOO_LARGE("파일 크기 초과"), //HttpStatus.PAYLOAD_TOO_LARGE,

    ;
    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
