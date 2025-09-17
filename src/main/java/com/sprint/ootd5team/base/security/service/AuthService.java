package com.sprint.ootd5team.base.security.service;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import com.sprint.ootd5team.base.exception.OotdException;
import com.sprint.ootd5team.base.security.OotdUserDetails;
import com.nimbusds.jose.JOSEException;
import com.sprint.ootd5team.base.errorcode.ErrorCode;
import com.sprint.ootd5team.base.exception.OotdException;
import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.base.security.JwtInformation;
import com.sprint.ootd5team.base.security.JwtRegistry;
import com.sprint.ootd5team.base.security.JwtTokenProvider;
import com.sprint.ootd5team.base.security.OotdUserDetails;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import com.sprint.ootd5team.domain.user.dto.request.ResetPasswordRequest;
import com.sprint.ootd5team.domain.user.dto.request.UserRoleUpdateRequest;
import java.util.UUID;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.mapper.UserMapper;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {


    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtTokenProvider tokenProvider;
    private final JwtRegistry jwtRegistry;
    private final UserDetailsService userDetailsService;
    private final JavaMailSender mailSender;


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
        user.updateRole(Role.valueOf(request.role()));
        return userMapper.toDto(userRepository.save(user));
    }

    /**
     * 이메일을 통해 비밀번호를 리셋한다.
     * 3분동안만 임시 비밀번호가 발급된다.
     * @param request 비밀번호 리셋을 원하는 이메일
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.email()).orElseThrow((UserNotFoundException::new));
        user.issueTemporaryPassword();
        userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("sprtms5335@gmail.com");
        message.setTo(user.getEmail());
        message.setSubject("[OOTD] 임시 비밀번호 안내");
        message.setText("안녕하세요, " + user.getName() + "님.\n\n"
            + "임시 비밀번호는 아래와 같습니다:\n\n"
            + user.getTempPassword() + "\n\n"
            + "해당 비밀번호는 3분 동안만 유효합니다.\n"
            + "로그인 후 반드시 새 비밀번호로 변경해주세요.");
        mailSender.send(message);
    }

    /**
     * 르프레쉬 토큰 재발급 로직
     * @param refreshToken 재발급 받기 전 리프레쉬토큰
     * @return Jwt 정보
     */
    @Transactional
    public JwtInformation refreshToken(String refreshToken) {
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
