package com.sprint.ootd5team.base.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Security 관련 에러 코드
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "권한이 없는 사용자입니다."),
    INVALID_USER_CREDENTIALS(HttpStatus.UNAUTHORIZED, "잘못된 사용자 인증 정보입니다."),
    UNSUPPORTED_PRINCIPAL(HttpStatus.UNAUTHORIZED, "지원하지 않는 사용자 인증 타입입니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST,"잘못된 토큰입니다."),
    INVALID_USER_DETAILS(HttpStatus.BAD_REQUEST,"잘못된 유저 Details입니다."),

    // User 관련 에러코드
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    USER_ALREADY_EXIST(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),

    // Feed 관련 에러 코드
    FEED_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 피드입니다."),
    INVALID_SORT_OPTION(HttpStatus.BAD_REQUEST, "유효하지 않은 정렬 옵션입니다."),

    // File 관련 에러코드
    FILE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 실패 - 재시도 가능"),
    FILE_PERMANENT_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 실패 - 모든 재시도 소진"),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제 실패"),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "파일 크기 초과"),

    //기타 에러코드
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"알 수 없는 오류가 발생했습니다."),

    // Clothes 관련 에러코드
    CLOTHES_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 의상입니다."),
    CLOTHES_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "옷 등록에 실패했습니다."),


    ;
    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
