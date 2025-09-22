package com.sprint.ootd5team.base.exception;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import com.sprint.ootd5team.base.exception.file.FileTooLargeException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

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

}