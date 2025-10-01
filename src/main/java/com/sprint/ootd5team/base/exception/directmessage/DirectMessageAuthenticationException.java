package com.sprint.ootd5team.base.exception.directmessage;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class DirectMessageAuthenticationException extends DirectMessageException {

    private DirectMessageAuthenticationException() {
        super(ErrorCode.DIRECT_MESSAGE_AUTHENTICATION_FAILED);
    }

    public static DirectMessageAuthenticationException missingAuthentication() {
        DirectMessageAuthenticationException exception = new DirectMessageAuthenticationException();
        exception.addDetail("reason", "AUTHENTICATION_MISSING");
        return exception;
    }

    public static DirectMessageAuthenticationException invalidPrincipalType(Object principal) {
        DirectMessageAuthenticationException exception = new DirectMessageAuthenticationException();
        exception.addDetail("reason", "INVALID_PRINCIPAL_TYPE");
        exception.addDetail("principalType", principal == null ? "null" : principal.getClass().getName());
        return exception;
    }

    public static DirectMessageAuthenticationException jwtWithoutIdentifiers() {
        DirectMessageAuthenticationException exception = new DirectMessageAuthenticationException();
        exception.addDetail("reason", "JWT_IDENTIFIER_MISSING");
        return exception;
    }

    public static DirectMessageAuthenticationException authenticationNameNotUuid(String authName) {
        DirectMessageAuthenticationException exception = new DirectMessageAuthenticationException();
        exception.addDetail("reason", "AUTHENTICATION_NAME_NOT_UUID");
        exception.addDetail("authenticationName", authName);
        return exception;
    }

    public static DirectMessageAuthenticationException unresolvablePrincipal(Object principal) {
        return unresolvablePrincipal(principal, null);
    }

    public static DirectMessageAuthenticationException unresolvablePrincipal(Object principal, String authName) {
        DirectMessageAuthenticationException exception = new DirectMessageAuthenticationException();
        exception.addDetail("reason", "UNRESOLVABLE_PRINCIPAL");
        exception.addDetail("principalType", principal == null ? "null" : principal.getClass().getName());
        if (principal instanceof CharSequence sequence) {
            exception.addDetail("principalValue", sequence.toString());
        }
        if (authName != null && !authName.isBlank()) {
            exception.addDetail("authenticationName", authName);
        }
        return exception;
    }
}