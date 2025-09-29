package com.sprint.ootd5team.base.exception;

import com.sprint.ootd5team.base.exception.file.FileTooLargeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.security.access.AccessDeniedException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OotdException.class)
    public ResponseEntity<ErrorResponse> handleOotdException(OotdException ex) {
        return ResponseEntity
            .status(ex.getErrorCode().getStatus())
            .body(new ErrorResponse(ex));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(ex));
    }

    /**
     * Spring 전역에서 multipart 업로드 크기 초과 시 발생
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public void translateAndRethrow(MaxUploadSizeExceededException ex) {
        throw FileTooLargeException.withSize(
            -1, // 알 수 없음 placeholder,
            ex.getMaxUploadSize()
        );
    }

    /* 공통 - PathVariable 파라미터 누락/오류 */
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ErrorResponse> handleMissingPathVar(
        MissingPathVariableException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ex));
    }

    /* 공통 - @Valid 위반 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST) // 400
            .body(new ErrorResponse(ex));
    }

    /* 공통 - 요청 필수 파라미터 누락 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
        MissingServletRequestParameterException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST) // 400
            .body(new ErrorResponse(ex));
    }

    // 인가 실패(권한 부족)일 때 실행
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
        AccessDeniedException ex
    ){
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN) // 403
            .body(new ErrorResponse(ex));
    }

    // 인증 실패(로그인시 사용자를 찾을수없음 or 비밀번호 아이디 일치 오류)일 때 실행
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
        BadCredentialsException ex
    ){
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED) // 401
            .body(new ErrorResponse(ex));
    }

    // 계정 잠김시 발생하는 예외
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLockedException(
        LockedException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.LOCKED) // 423
            .body(new ErrorResponse(ex));
    }
}