package com.sprint.ootd5team.domain.user.service;

import com.sprint.ootd5team.base.exception.user.UserAlreadyExistException;
import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.base.security.JwtRegistry;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import com.sprint.ootd5team.domain.user.dto.request.ChangePasswordRequest;
import com.sprint.ootd5team.domain.user.dto.request.UserCreateRequest;
import com.sprint.ootd5team.domain.user.dto.request.UserLockUpdateRequest;
import com.sprint.ootd5team.domain.user.dto.response.UserDtoCursorResponse;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.mapper.UserMapper;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import com.sprint.ootd5team.domain.user.repository.UserRepositoryCustom;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final UserRepositoryCustom userRepositoryCustom;
    private final JwtRegistry jwtRegistry;


    /**
     * 유저를 생성하는 메서드
     * @param request 유저 생성 정보(이메일, 닉네임, 비밀번호)
     * @return 생성된 유저 정보
     */
    @Transactional
    public UserDto create(UserCreateRequest request){

        log.debug("[User]유저 생성 시작");
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistException();
        }
        User user = userRepository.save(
            new User(request.name(), request.email(), passwordEncoder.encode(request.password()),
                Role.USER));

        // 프로필 생성해서 저장
        Profile profile = new Profile(user,user.getName(),null,null,null,null,null);
        profileRepository.save(profile);
        log.info("[User]유저 생성 완료 profile:{}",profile);

        return userMapper.toDto(user);
    }

    /**
     * 비밀번호를 바꾸는 메서드
     * @param userId 바꾸고자 하는 유저의 Id
     * @param request 바꿀 비밀번호
     */
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        log.debug("[User]비밀번호 변경 시작");
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.updatePassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        log.info("[User]비밀번호 변경 완료 유저ID:{}",userId);
    }

    /**
     * (어드민전용) 커서 페이지네이션을 사용하여 사용자 정보를 가져오는 메서드
     * @param cursor 참조할 커서 정보
     * @param idAfter 다음 필드의 UUID
     * @param limit 1페이지당 가져올 개수
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방법
     * @param emailLike 유사한 이메일 검색
     * @param roleEqual 역할 (ADMIN, USER)
     * @param locked 잠금여부
     * @return UserDtoCursorResponse 커서 페이지네이션 결과
     */
    @Transactional
    public UserDtoCursorResponse getUsers(
        String cursor,
        UUID idAfter,
        Integer limit,
        String sortBy,
        String sortDirection,
        String emailLike,
        String roleEqual,
        Boolean locked) {

        log.debug("[User]유저 검색 커서 기반 페이지네이션 시작");
        List<UserDto> allByCursor = userRepositoryCustom.findUsersWithCursor(cursor, idAfter, limit+1,
                sortBy, sortDirection, emailLike, roleEqual, locked)
            .stream()
            .map(userMapper::toDto)
            .collect(Collectors.toList());

        // 검색된 리스트 사이즈(limit+1 검색)가 limit 보다 클경우 hasNext true
        boolean hasNext = allByCursor.size() > limit;

        String nextCursor = null;
        UUID nextIdAfter = null;
        if(hasNext) {
            // 마지막 인덱스 추출(다음)
            UserDto userDto = allByCursor.get(allByCursor.size() - 1);

            // 다음 인덱스 UUID
            nextIdAfter = userDto.id();

            // 다음 커서
            if(sortBy.equals("createdAt")){
                nextCursor = userDto.createdAt().toString();
            }
            else{
                nextCursor = userDto.email();
            }

        }

        // 전체 갯수
        Long totalCount = userRepositoryCustom.countUsers(roleEqual, emailLike, locked);

        // 다음 페이지가 존재할 때만 limit+1을 검색했기때문에 마지막 인덱스 제거
        if(hasNext) allByCursor.remove(allByCursor.size() - 1);

        log.info("[User]유저 검색 커서 기반 페이지네이션 완료");
        return new UserDtoCursorResponse(allByCursor, nextCursor, nextIdAfter, hasNext, totalCount, sortBy,
            sortDirection);
    }

    /**
     * 계정 잠금여부 업데이트 메서드
     * @param userId 업데이트할 UserId
     * @param request 계정잠금 요청(false: 잠금해제, true: 잠금)
     * @return 변경된 userDto
     */
    @Transactional
    @PreAuthorize( "hasRole('ADMIN')")
    public UserDto updateUserLock(UUID userId, @Valid UserLockUpdateRequest request){

        log.debug("[User]계정 잠금여부 업데이트 메서드 시작 userId:{}, request:{}", userId, request);
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.updateLock(request.locked());
        User save = userRepository.save(user);
        jwtRegistry.invalidateJwtInformationByUserId(userId);
        log.info("[User] 계정잠금 완료userId:{}, request:{}", userId, request);
        return userMapper.toDto(save);

    }
}
