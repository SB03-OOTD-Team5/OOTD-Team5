package com.sprint.ootd5team.base.exception.user;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import java.util.UUID;

public class UserNotFoundException extends UserException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }

    public static UserNotFoundException withId(UUID userId) {
        UserNotFoundException exception = new UserNotFoundException();
        exception.addDetail("userId", userId);
        return exception;
    }

    public static UserNotFoundException withUsername(String username) {
        UserNotFoundException exception = new UserNotFoundException();
        exception.addDetail("username", username);
        return exception;
    }
}
