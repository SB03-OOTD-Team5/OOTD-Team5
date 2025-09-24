package com.sprint.ootd5team.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.exception.user.UserAlreadyExistException;
import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.domain.profile.dto.request.ProfileDto;
import com.sprint.ootd5team.domain.profile.service.ProfileService;
import com.sprint.ootd5team.domain.user.controller.UserController;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import com.sprint.ootd5team.domain.user.dto.request.ChangePasswordRequest;
import com.sprint.ootd5team.domain.user.dto.request.UserCreateRequest;
import com.sprint.ootd5team.domain.user.dto.response.UserDtoCursorResponse;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.service.UserService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@DisplayName("UserController 테스트")
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles({"test", "securitytest"})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ProfileService profileService;

    private UserDto testUserDto;
    private UserCreateRequest userCreateRequest;
    private ChangePasswordRequest changePasswordRequest;
    private ProfileDto testProfileDto;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testUserDto = new UserDto(
            testUserId,
            Instant.now(),
            "test@example.com",
            "testUser",
            Role.USER,
null,
            false
        );

        userCreateRequest = new UserCreateRequest(
            "testUser",
            "test@example.com",
            "password123"
        );

        changePasswordRequest = new ChangePasswordRequest("newPassword123");

        testProfileDto = new ProfileDto(
            testUserId,
            "testUser",
            "male",
            LocalDate.now(),
            null, 2, "image.jpg");
    }

    @Test
    @DisplayName("유저 생성 성공")
    void createUser_Success() throws Exception {
        // given
        given(userService.create(any(UserCreateRequest.class))).willReturn(testUserDto);

        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateRequest)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(testUserDto.id().toString()))
            .andExpect(jsonPath("$.name").value(testUserDto.name()))
            .andExpect(jsonPath("$.email").value(testUserDto.email()))
            .andExpect(jsonPath("$.role").value(testUserDto.role().toString()))
            .andExpect(jsonPath("$.locked").value(testUserDto.locked()));
    }

    @Test
    @DisplayName("유저 생성 실패 - 이메일 중복")
    void createUser_Fail_EmailAlreadyExists() throws Exception {
        // given
        given(userService.create(any(UserCreateRequest.class)))
            .willThrow(new UserAlreadyExistException());

        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateRequest)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유저 생성 실패 - 유효성 검증 실패 (빈 이메일)")
    void createUser_Fail_ValidationError_EmptyEmail() throws Exception {
        // given
        UserCreateRequest invalidRequest = new UserCreateRequest(
            "testUser",
            "", // 빈 이메일
            "password123"
        );

        System.out.println(invalidRequest);


        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유저 생성 실패 - 유효성 검증 실패 (잘못된 이메일 형식)")
    void createUser_Fail_ValidationError_InvalidEmailFormat() throws Exception {
        // given
        UserCreateRequest invalidRequest = new UserCreateRequest(
            "testUser",
            "invalid-email", // 잘못된 이메일 형식
            "password123"
        );

        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_Success() throws Exception {
        // given
        doNothing().when(userService).changePassword(eq(testUserId), any(ChangePasswordRequest.class));

        // when & then
        mockMvc.perform(patch("/api/users/{userId}/password", testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordRequest)))
            .andDo(print())
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 사용자 없음")
    void changePassword_Fail_UserNotFound() throws Exception {
        // given
        willThrow(new UserNotFoundException())
            .given(userService).changePassword(eq(testUserId), any(ChangePasswordRequest.class));

        // when & then
        mockMvc.perform(patch("/api/users/{userId}/password", testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordRequest)))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 유효성 검증 실패 (빈 비밀번호)")
    void changePassword_Fail_ValidationError_EmptyPassword() throws Exception {
        // given
        ChangePasswordRequest invalidRequest = new ChangePasswordRequest(""); // 빈 비밀번호

        // when & then
        mockMvc.perform(patch("/api/users/{userId}/password", testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("사용자 프로필 조회 성공")
    void getUserProfile_Success() throws Exception {
        // given
        given(profileService.getProfile(testUserId)).willReturn(testProfileDto);

        // when & then
        mockMvc.perform(get("/api/users/{userId}/profiles", testUserId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.userId").value(testProfileDto.userId().toString()))
            .andExpect(jsonPath("$.name").value(testProfileDto.name()))
            .andExpect(jsonPath("$.birthDate").value(testProfileDto.birthDate().toString()))
            .andExpect(jsonPath("$.profileImageUrl").value(testProfileDto.profileImageUrl()));
    }

    @Test
    @DisplayName("사용자 프로필 조회 실패 - 사용자 없음")
    void getUserProfile_Fail_UserNotFound() throws Exception {
        // given
        given(profileService.getProfile(testUserId))
            .willThrow(new UserNotFoundException());

        // when & then
        mockMvc.perform(get("/api/users/{userId}/profiles", testUserId))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("사용자 목록 조회 성공 - 기본 파라미터")
    void getUsers_Success_BasicParameters() throws Exception {
        // given
        List<UserDto> users = Arrays.asList(testUserDto);
        UserDtoCursorResponse response = new UserDtoCursorResponse(
            users,
            "next-cursor",
            UUID.randomUUID(),
            true,
            10L,
            "createdAt",
            "DESC"
        );

        given(userService.getUsers(
            null, null, 10, "createdAt", "DESC", null, null, null))
            .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/users")
                .param("limit", "10")
                .param("sortBy", "createdAt")
                .param("sortDirection", "DESC"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].id").value(testUserDto.id().toString()))
            .andExpect(jsonPath("$.data[0].name").value(testUserDto.name()))
            .andExpect(jsonPath("$.data[0].email").value(testUserDto.email()))
            .andExpect(jsonPath("$.hasNext").value(true))
            .andExpect(jsonPath("$.totalCount").value(10))
            .andExpect(jsonPath("$.sortBy").value("createdAt"))
            .andExpect(jsonPath("$.sortDirection").value("DESC"));
    }

    @Test
    @DisplayName("사용자 목록 조회 성공 - 전체 파라미터 포함")
    void getUsers_Success_AllParameters() throws Exception {
        // given
        String cursor = "2024-01-01T00:00:00";
        UUID idAfter = UUID.randomUUID();
        String emailLike = "test";
        String roleEqual = "USER";
        Boolean locked = false;

        List<UserDto> users = Arrays.asList(testUserDto);
        UserDtoCursorResponse response = new UserDtoCursorResponse(
            users,
            "next-cursor",
            UUID.randomUUID(),
            false,
            5L,
            "email",
            "ASC"
        );

        given(userService.getUsers(cursor, idAfter, 5, "email", "ASC", emailLike, roleEqual, locked))
            .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/users")
                .param("cursor", cursor)
                .param("idAfter", idAfter.toString())
                .param("limit", "5")
                .param("sortBy", "email")
                .param("sortDirection", "ASC")
                .param("emailLike", emailLike)
                .param("roleEqual", roleEqual)
                .param("locked", locked.toString()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.hasNext").value(false))
            .andExpect(jsonPath("$.totalCount").value(5))
            .andExpect(jsonPath("$.sortBy").value("email"))
            .andExpect(jsonPath("$.sortDirection").value("ASC"));
    }

    @Test
    @DisplayName("사용자 목록 조회 성공 - 빈 결과")
    void getUsers_Success_EmptyResult() throws Exception {
        // given
        UserDtoCursorResponse response = new UserDtoCursorResponse(
            Arrays.asList(),
            null,
            null,
            false,
            0L,
            "createdAt",
            "DESC"
        );

        given(userService.getUsers(
            null, null, 10, "createdAt", "DESC", null, null, null))
            .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/users")
                .param("limit", "10")
                .param("sortBy", "createdAt")
                .param("sortDirection", "DESC"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty())
            .andExpect(jsonPath("$.hasNext").value(false))
            .andExpect(jsonPath("$.totalCount").value(0))
            .andExpect(jsonPath("$.nextCursor").isEmpty())
            .andExpect(jsonPath("$.nextIdAfter").isEmpty());
    }

    @Test
    @DisplayName("사용자 목록 조회 실패 - 필수 파라미터 누락 (limit)")
    void getUsers_Fail_MissingRequiredParameter_Limit() throws Exception {
        // when & then
        mockMvc.perform(get("/api/users")
                .param("sortBy", "createdAt")
                .param("sortDirection", "DESC"))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("사용자 목록 조회 실패 - 필수 파라미터 누락 (sortBy)")
    void getUsers_Fail_MissingRequiredParameter_SortBy() throws Exception {
        // when & then
        mockMvc.perform(get("/api/users")
                .param("limit", "10")
                .param("sortDirection", "DESC"))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("사용자 목록 조회 실패 - 필수 파라미터 누락 (sortDirection)")
    void getUsers_Fail_MissingRequiredParameter_SortDirection() throws Exception {
        // when & then
        mockMvc.perform(get("/api/users")
                .param("limit", "10")
                .param("sortBy", "createdAt"))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }
}
