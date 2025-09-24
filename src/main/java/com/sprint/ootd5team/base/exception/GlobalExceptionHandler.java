package com.sprint.ootd5team.base.exception;

import com.sprint.ootd5team.base.exception.file.FileTooLargeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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


    // Valid 위반시 실행
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST) // 400
            .body(new ErrorResponse(ex));
    }

    // 필수 파라미터가 존재하지 않을때 실행
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
        MissingServletRequestParameterException ex
    ){
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
}