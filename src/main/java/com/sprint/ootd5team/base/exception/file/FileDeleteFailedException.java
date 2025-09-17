package com.sprint.ootd5team.base.exception.file;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class FileDeleteFailedException extends FileException{

    public FileDeleteFailedException() {
        super(ErrorCode.FILE_DELETE_FAILED);
    }

    public FileDeleteFailedException(String filePath) {
        super(ErrorCode.FILE_DELETE_FAILED);
        addDetail("filePath", filePath);
    }

    public static FileDeleteFailedException withFilePath(String filePath) {
        return new FileDeleteFailedException(filePath);
    }
}
