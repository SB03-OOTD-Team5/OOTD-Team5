package com.sprint.ootd5team.base.security;


import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.mapper.UserMapper;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OotdUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username).orElseThrow(UserNotFoundException::new);

        UserDto dto = userMapper.toDto(user);

        return new OotdSecurityUserDetails(dto, user.getPassword());
    }

    /**
     * 임시 비밀번호 검증용 메서드
     */
    @Transactional
    public boolean authenticateTemporaryPassword(String email, String tempPassword) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if(optionalUser.isEmpty()) return false;

        User user = optionalUser.get();
        boolean valid = user.validateTemporaryPassword(tempPassword);
        if(valid) {
            // 로그인 성공 후 임시 비밀번호 파기
            user.clearTemporaryPassword();
            userRepository.save(user);
        }
        return valid;
    }
}
