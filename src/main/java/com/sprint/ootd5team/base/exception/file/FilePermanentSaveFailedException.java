package com.sprint.ootd5team.base.exception.file;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class FilePermanentSaveFailedException extends FileException {

    public FilePermanentSaveFailedException() {
        super(ErrorCode.FILE_PERMANENT_SAVE_FAILED);
    }

    public static FilePermanentSaveFailedException withFileName(String fileName) {
        FilePermanentSaveFailedException exception = new FilePermanentSaveFailedException();
        exception.addDetail("fileName", fileName);
        return exception;
    }
}
