package com.sprint.ootd5team.base.exception.file;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class FileTooLargeException extends FileException {

    public FileTooLargeException() {
        super(ErrorCode.FILE_TOO_LARGE);
    }

    public static FileTooLargeException withSize(long fileSize, long maxSize) {
        FileTooLargeException exception = new FileTooLargeException();
        exception.addDetail("fileSize", fileSize);
        exception.addDetail("maxSize", maxSize);
        return exception;
    }
}
