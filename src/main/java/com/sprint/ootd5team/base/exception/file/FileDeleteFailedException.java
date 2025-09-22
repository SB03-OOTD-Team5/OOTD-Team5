package com.sprint.ootd5team.base.exception.file;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class FileDeleteFailedException extends FileException {

    public FileDeleteFailedException() {
        super(ErrorCode.FILE_DELETE_FAILED);
    }

    public static FileDeleteFailedException withFilePath(String filePath) {
        FileDeleteFailedException exception = new FileDeleteFailedException();
        exception.addDetail("filePath", filePath);
        return exception;
    }

    public static FileDeleteFailedException withFilePath(String filePath, Throwable cause) {
        FileDeleteFailedException exception = new FileDeleteFailedException();
        exception.addDetail("filePath", filePath);
        exception.initCause(cause);
        return exception;
    }
}
