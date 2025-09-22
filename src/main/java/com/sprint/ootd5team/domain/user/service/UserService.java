package com.sprint.ootd5team.domain.user.service;

import com.sprint.ootd5team.base.exception.user.UserAlreadyExistException;
import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import com.sprint.ootd5team.domain.user.dto.request.ChangePasswordRequest;
import com.sprint.ootd5team.domain.user.dto.request.UserCreateRequest;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.mapper.UserMapper;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
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

        // 프로필 생성해서 저장
        Profile profile = new Profile(user.getId(),user.getName(),null,null,null,null,null,null,null,null,null);
        profileRepository.save(profile);

        return userMapper.toDto(user);
    }

    /**
     * 비밀번호를 바꾸는 메서드
     * @param userId 바꾸고자 하는 유저의 Id
     * @param request 바꿀 비밀번호
     */
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.updatePassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }
}
