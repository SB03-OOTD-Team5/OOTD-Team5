package com.sprint.ootd5team.domain.user.service;

import com.sprint.ootd5team.base.exception.user.UserAlreadyExistException;
import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import com.sprint.ootd5team.domain.user.dto.request.UserCreateRequest;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.mapper.UserMapper;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


    /**
     * 유저를 생성하는 메서드
     * @param request 유저 생성 정보(이메일, 닉네임, 비밀번호)
     * @return 생성된 유저 정보
     */
    @Transactional
    public UserDto create(UserCreateRequest request){

        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistException();
        }
        User user = userRepository.save(
            new User(request.name(), request.email(), passwordEncoder.encode(request.password()),
                Role.USER));

        return userMapper.toDto(user);
    }
}
