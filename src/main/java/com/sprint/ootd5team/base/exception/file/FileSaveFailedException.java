package com.sprint.ootd5team.base.exception.file;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class FileSaveFailedException extends FileException {

    public FileSaveFailedException() {
        super(ErrorCode.FILE_SAVE_FAILED);
    }

    public static FileSaveFailedException withFileName(String fileName) {
        FileSaveFailedException exception = new FileSaveFailedException();
        exception.addDetail("fileName", fileName);
        return exception;
    }

    public static FileSaveFailedException withFileName(String fileName, Throwable cause) {
        FileSaveFailedException exception = new FileSaveFailedException();
        exception.addDetail("fileName", fileName);
        exception.initCause(cause);
        return exception;
    }
}
