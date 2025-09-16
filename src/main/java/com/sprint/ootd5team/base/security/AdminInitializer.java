package com.sprint.ootd5team.base.security;

import com.sprint.ootd5team.base.exception.user.UserAlreadyExistException;
import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import com.sprint.ootd5team.domain.user.dto.request.UserCreateRequest;
import com.sprint.ootd5team.domain.user.dto.request.UserRoleUpdateRequest;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!test")
@Slf4j
@RequiredArgsConstructor
@Component
public class AdminInitializer implements ApplicationRunner {

    @Value("${ootd.admin.username}")
    private String username;
    @Value("${ootd.admin.password}")
    private String password;
    @Value("${ootd.admin.email}")
    private String email;

    private final UserService userService;
    private final AuthService authService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 관리자 계정 초기화 로직
        UserCreateRequest request = new UserCreateRequest(username,email,password);

        try {
            UserDto admin = userService.create(request);
            authService.updateRoleInternal(new UserRoleUpdateRequest(Role.ADMIN.name()));
            log.info("관리자 계정이 성공적으로 생성되었습니다.");
        } catch (UserAlreadyExistException e) {
            log.warn("관리자 계정이 이미 존재합니다");
        } catch (Exception e) {
            log.error("관리자 계정 생성 중 오류가 발생했습니다.: {}", e.getMessage());
        }
    }
}
