package com.sprint.ootd5team.domain.user.controller;

import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.domain.profile.dto.data.ProfileUpdateRequest;
import com.sprint.ootd5team.domain.profile.dto.request.ProfileDto;
import com.sprint.ootd5team.domain.profile.service.ProfileService;
import com.sprint.ootd5team.domain.user.controller.api.UserApi;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import com.sprint.ootd5team.domain.user.dto.request.ChangePasswordRequest;
import com.sprint.ootd5team.domain.user.dto.request.UserCreateRequest;
import com.sprint.ootd5team.domain.user.dto.request.UserLockUpdateRequest;
import com.sprint.ootd5team.domain.user.dto.request.UserRoleUpdateRequest;
import com.sprint.ootd5team.domain.user.dto.response.UserDtoCursorResponse;
import com.sprint.ootd5team.domain.user.service.UserService;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.http.HttpStatusCode;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;
    private final ProfileService profileService;
    private final AuthService authService;

    @Override
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid  @RequestBody UserCreateRequest request) {
        UserDto userDto = userService.create(request);
        return ResponseEntity
            .status(HttpStatusCode.CREATED)
            .body(userDto);
    }

    @Override
    @PatchMapping("/{userId}/password")
    public ResponseEntity<Void> changePassword(@PathVariable UUID userId,
        @Valid @RequestBody ChangePasswordRequest request){

        userService.changePassword(userId,request);

        return ResponseEntity
            .status(HttpStatusCode.NO_CONTENT)
            .build();

    }

    @Override
    @GetMapping("/{userId}/profiles")
    public ResponseEntity<ProfileDto> getUserProfile(@PathVariable UUID userId){

        ProfileDto profile = profileService.getProfile(userId);

        return ResponseEntity
            .status(HttpStatusCode.OK)
            .body(profile);
    }

    @GetMapping
    public ResponseEntity<UserDtoCursorResponse> getUsers(
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) UUID idAfter,
        @RequestParam Integer limit,
        @RequestParam String sortBy,
        @RequestParam String sortDirection,
        @RequestParam(required = false) String emailLike,
        @RequestParam(required = false) String roleEqual,
        @RequestParam(required = false) Boolean locked
    ) {
        UserDtoCursorResponse response = userService.getUsers(cursor,idAfter,limit,sortBy,sortDirection,emailLike,roleEqual,locked);

        return ResponseEntity
            .status(HttpStatusCode.OK)
            .body(response);
    }


    @Override
    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable UUID userId, @RequestBody UserRoleUpdateRequest request) {
        UserDto userDto = authService.updateRoleInternal(userId, request);

        return ResponseEntity
            .status(HttpStatusCode.OK)
            .body(userDto);
    }

    @Override
    @PatchMapping(value = "/{userId}/profiles",
    consumes = { MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ProfileDto> updateUserProfile(
        @PathVariable UUID userId,
        @RequestPart @Valid ProfileUpdateRequest request,
        @RequestPart(value = "image",required = false) MultipartFile image) {

        Optional<MultipartFile> profileImageRequest = Optional.ofNullable(image);

        ProfileDto profileDto = profileService.updateProfile(userId, request, profileImageRequest);

        return ResponseEntity
            .status(HttpStatusCode.OK)
            .body(profileDto);
    }

    @Override
    @PatchMapping("/{userId}/lock")
    public ResponseEntity<UserDto> updateUserLock(
        @PathVariable UUID userId,
        @RequestBody UserLockUpdateRequest request) {

        UserDto userDto = userService.updateUserLock(userId, request);

        return ResponseEntity
            .status(HttpStatusCode.OK)
            .body(userDto);
    }
}
