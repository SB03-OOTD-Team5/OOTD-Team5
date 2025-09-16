package com.sprint.ootd5team.base.security.service;

import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import com.sprint.ootd5team.domain.user.dto.request.UserLockUpdateRequest;
import com.sprint.ootd5team.domain.user.dto.request.UserRoleUpdateRequest;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.mapper.UserMapper;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * 역할 업데이트 메서드(
     *
     * @param request 역할 바꾸길 원하는 유저의 역할
     * @param userId  유저 ID
     */
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateRoleInternal(UUID userId, UserRoleUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.updateRole(Role.valueOf(request.role()));
        return userMapper.toDto(userRepository.save(user));
    }


    @PreAuthorize("hasRole('ADMIN')")
    public UserDto lockUser(UUID userId, UserLockUpdateRequest request) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        return userMapper.toDto(user);
    }
}
