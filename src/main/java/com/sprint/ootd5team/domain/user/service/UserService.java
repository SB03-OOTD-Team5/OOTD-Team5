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
     * 유저 생성 로직 (임시)
     */
    public UserDto create(UserCreateRequest request){
        return null;
    }
}
