package com.sprint.ootd5team.domain.user.controller.api;

import com.sprint.ootd5team.base.exception.ErrorResponse;
import com.sprint.ootd5team.domain.profile.dto.request.ProfileDto;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import com.sprint.ootd5team.domain.user.dto.request.ChangePasswordRequest;
import com.sprint.ootd5team.domain.user.dto.request.UserCreateRequest;
import com.sprint.ootd5team.domain.user.dto.response.UserDtoCursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "User", description = "사용자 계정 관련 API")
public interface UserApi {
    @Operation(
        summary = "계정 목록 조회",
        description = "cursor, idAfter, limit, sortBy, sortDirection, emailLike, roleEqual, locked 등의 파라미터를 이용해 계정 목록을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "계정 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDtoCursorResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "계정 목록 조회 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ResponseEntity<UserDtoCursorResponse> getUsers(
        @Parameter(description = "커서 기반 페이지네이션을 위한 cursor") String cursor,

        @Parameter(description = "idAfter 값 (UUID)") UUID idAfter,

        @Parameter(description = "조회 개수 (기본값: 20)") Integer limit,

        @Parameter(description = "정렬 기준", example = "email",
            schema = @Schema(allowableValues = {"email", "createdAt"})) String sortBy,

        @Parameter(description = "정렬 방향", example = "ASCENDING",
            schema = @Schema(allowableValues = {"ASCENDING", "DESCENDING"})) String sortDirection,

        @Parameter(description = "이메일 LIKE 검색") String emailLike,

        @Parameter(description = "역할 필터링", schema = @Schema(allowableValues = {"USER", "ADMIN"})) String roleEqual,

        @Parameter(description = "계정 잠금 여부") Boolean locked
    );



    @Operation(
        summary = "사용자 등록(회원가입)",
        description = "사용자 정보를 입력받아 새로운 계정을 생성합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "사용자 등록(회원가입) 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "사용자 등록(회원가입) 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ResponseEntity<UserDto> createUser(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "회원가입 요청 바디",
        required = true,
        content = @Content(
            schema = @Schema(implementation = UserCreateRequest.class)
        )
    )
    UserCreateRequest request);


    @Operation(
        summary = "프로필 조회",
        description = "userId를 이용해 특정 사용자의 프로필 정보를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "프로필 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProfileDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "프로필 조회 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ResponseEntity<ProfileDto> getUserProfile(
        @Parameter(
            description = "조회할 사용자 ID (UUID)",
            required = true,
            example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID userId
    );



    @Operation(
        summary = "비밀번호 변경",
        description = "특정 사용자의 비밀번호를 변경합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "비밀번호 변경 성공"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "비밀번호 변경 실패 (잘못된 요청)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "비밀번호 변경 실패 (사용자 없음)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ResponseEntity<Void> changePassword(
        @Parameter(
            description = "비밀번호를 변경할 사용자 ID (UUID)",
            required = true,
            example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID userId,

        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "새 비밀번호 요청 바디",
            required = true,
            content = @Content(
                schema = @Schema(implementation = ChangePasswordRequest.class)
            )
        )
        ChangePasswordRequest request);
}
