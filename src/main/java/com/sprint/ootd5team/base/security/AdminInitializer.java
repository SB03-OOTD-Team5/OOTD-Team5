package com.sprint.ootd5team.base.security;

import ch.qos.logback.classic.spi.IThrowableProxy;
import com.sprint.ootd5team.base.exception.user.UserAlreadyExistException;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 관리자 계정 초기화 로직
        try {
            userRepository.findByEmail(email).ifPresent(user -> {throw new UserAlreadyExistException();});
            User user = userRepository.save(new User(username, email, passwordEncoder.encode(password), Role.ADMIN));
            profileRepository.save(new Profile(user,user.getName(),null,null,null,null,null));
            log.info("관리자 계정이 성공적으로 생성되었습니다.");
        } catch (UserAlreadyExistException e) {
            log.warn("관리자 계정이 이미 존재합니다");
        } catch (Exception e) {
            log.error("관리자 계정 생성 중 오류가 발생했습니다.: {}", e.getMessage());
        }
    }
}
