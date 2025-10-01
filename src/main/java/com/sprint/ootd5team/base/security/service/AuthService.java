package com.sprint.ootd5team.base.security.service;

import com.nimbusds.jose.JOSEException;
import com.sprint.ootd5team.base.errorcode.ErrorCode;
import com.sprint.ootd5team.base.exception.OotdException;
import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.base.security.JwtInformation;
import com.sprint.ootd5team.base.security.JwtRegistry;
import com.sprint.ootd5team.base.security.JwtTokenProvider;
import com.sprint.ootd5team.base.security.OotdUserDetails;
import com.sprint.ootd5team.domain.notification.event.type.single.RoleUpdatedEvent;
import com.sprint.ootd5team.domain.user.dto.TemporaryPasswordCreatedEvent;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import com.sprint.ootd5team.domain.user.dto.request.ResetPasswordRequest;
import com.sprint.ootd5team.domain.user.dto.request.UserRoleUpdateRequest;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.mapper.UserMapper;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {


    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtTokenProvider tokenProvider;
    private final JwtRegistry jwtRegistry;
    private final UserDetailsService userDetailsService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 역할 업데이트 메서드(어드민)
     *
     * @param request 역할 바꾸길 원하는 유저의 역할
     * @param userId  유저 ID
     * @return 변경된 유저의 Dto
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserDto updateRoleInternal(UUID userId, UserRoleUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Role oldRole = user.getRole();
        Role newRole = Role.valueOf(request.role());
        user.updateRole(newRole);

        User save = userRepository.save(user);
        jwtRegistry.invalidateJwtInformationByUserId(userId);
        eventPublisher.publishEvent(new RoleUpdatedEvent(user.getId(), oldRole.name(), newRole.name()));
        return userMapper.toDto(save);
    }

    /**
     * 이메일을 통해 비밀번호를 리셋한다.
     * 3분동안만 임시 비밀번호가 발급된다.
     *
     * @param request 비밀번호 리셋을 원하는 이메일
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow((UserNotFoundException::new));
        user.issueTemporaryPassword();
        userRepository.save(user);

        // 이벤트 리스너를 통해 비동기로 메일 보내기를 처리함
        eventPublisher.publishEvent(
            new TemporaryPasswordCreatedEvent(user.getTempPassword(), user.getEmail(),
                user.getName(), user.getTempPasswordExpireAt()));
    }

    /**
     * 리프레쉬 토큰 재발급 로직
     *
     * @param refreshToken 재발급 받기 전 리프레쉬토큰
     * @return Jwt 정보
     */
    @Transactional
    public JwtInformation refreshToken(String refreshToken) {
        log.info("refreshToken : {}", refreshToken);
        // Validate refresh token
        if (!tokenProvider.validateRefreshToken(refreshToken)
            || !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
            throw new OotdException(ErrorCode.INVALID_TOKEN);
        }

        String username = tokenProvider.getEmailFromToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!(userDetails instanceof OotdUserDetails ootdUserDetails)) {
            throw new OotdException(ErrorCode.INVALID_USER_DETAILS);
        }

        try {
            String newAccessToken = tokenProvider.generateAccessToken(ootdUserDetails);
            String newRefreshToken = tokenProvider.generateRefreshToken(ootdUserDetails);

            JwtInformation newJwtInformation = new JwtInformation(
                ootdUserDetails.getUserDto(),
                newAccessToken,
                newRefreshToken
            );
            jwtRegistry.rotateJwtInformation(
                refreshToken,
                newJwtInformation
            );

            return newJwtInformation;

        } catch (JOSEException e) {
            throw new OotdException(ErrorCode.INTERNAL_SERVER_ERROR, e);
        }
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
