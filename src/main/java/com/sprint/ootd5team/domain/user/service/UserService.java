package com.sprint.ootd5team.domain.user.service;

import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import com.sprint.ootd5team.domain.user.dto.request.UserCreateRequest;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    /**
     * 이메일을 통해 비밀번호를 리셋한다.
     * 3분동안만 임시 비밀번호가 발급된다.
     * @param email 비밀번호 리셋을 원하는 이메일
     */
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow((UserNotFoundException::new));
        user.resetPassword();
    }

    /**
     * 유저 생성 로직 (임시)
     */
    public UserDto create(UserCreateRequest request){

    }
}
