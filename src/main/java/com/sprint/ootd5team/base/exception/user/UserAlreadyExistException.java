package com.sprint.ootd5team.base.exception.user;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import java.util.UUID;

public class UserAlreadyExistException extends UserException {
    public UserAlreadyExistException() {
        super(ErrorCode.USER_ALREADY_EXIST);
    }

    public static UserAlreadyExistException withId(UUID userId) {
        UserAlreadyExistException exception = new UserAlreadyExistException();
        exception.addDetail("userId", userId);
        return exception;
    }

    public static UserAlreadyExistException withUsername(String username) {
        UserAlreadyExistException exception = new UserAlreadyExistException();
        exception.addDetail("username", username);
        return exception;
    }
}
