package com.sprint.ootd5team.base.security.service;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import com.sprint.ootd5team.base.exception.OotdException;
import com.sprint.ootd5team.base.security.OotdUserDetails;
import com.sprint.ootd5team.domain.user.dto.request.UserRoleUpdateRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    /**
     * 역할 업데이트 메서드(임시)
     * @param userRoleUpdateRequest
     */
    public void updateRoleInternal(UserRoleUpdateRequest userRoleUpdateRequest) {
    }

    /**
     * 현재 인증된 사용자의 ID를 반환한다.
     *
     * @return 현재 로그인한 사용자의 UUID
     */
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new OotdException(ErrorCode.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof OotdUserDetails userDetails) {
            return userDetails.getUserId();
        }
        OotdException ex = new OotdException(ErrorCode.UNSUPPORTED_PRINCIPAL);
        ex.addDetail("principalType", principal.getClass().getName());
        throw ex;
    }
}