package com.sprint.ootd5team.base.security.oauth2;

import com.sprint.ootd5team.domain.oauthuser.entity.OauthUser;
import com.sprint.ootd5team.domain.oauthuser.repository.OauthRepository;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.mapper.UserMapper;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles({"test", "securitytest"})
class CustomOAuth2UserServiceTest {

    @Mock
    private OauthRepository oauthRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Mock
    private DefaultOAuth2UserService defaultOAuth2UserService;

    private OAuth2UserRequest oAuth2UserRequest;
    private Map<String, Object> googleAttributes;
    private Map<String, Object> kakaoAttributes;

    @BeforeEach
    void setUp() {
        // Google 속성 설정
        googleAttributes = new HashMap<>();
        googleAttributes.put("sub", "google123");
        googleAttributes.put("name", "홍길동");
        googleAttributes.put("email", "hong@gmail.com");

        // Kakao 속성 설정
        kakaoAttributes = new HashMap<>();
        kakaoAttributes.put("id", 12345L);
        Map<String, Object> kakaoAccount = new HashMap<>();
        Map<String, Object> profile = new HashMap<>();
        profile.put("nickname", "카카오홍길동");
        kakaoAccount.put("profile", profile);
        kakaoAttributes.put("kakao_account", kakaoAccount);

        ReflectionTestUtils.setField(customOAuth2UserService, "delegate", defaultOAuth2UserService);

    }

    @Test
    @DisplayName("구글 로그인 - 신규 사용자 생성")
    void loadUser_Google_NewUser() {
        // Given
        OAuth2UserRequest request = createOAuth2UserRequest("google");
        User newUser = new User("홍길동", "hong@gmail.com", null, Role.USER);
        ReflectionTestUtils.setField(newUser, "id", UUID.randomUUID());
        UserDto userDto = new UserDto(newUser.getId(), Instant.now(), "hong@gmail.com", "홍길동",Role.USER, List.of("google"), false);

        when(defaultOAuth2UserService.loadUser(any())).thenReturn(
            new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")), // 권한
                Map.of( // 유저 정보
                    "sub", "11223344556677889900", // 구글의 고유 ID 키
                    "name", "홍길동",
                    "email", "hong@gmail.com",
                    "picture", "https://lh3.googleusercontent.com/a/AEdFTp4.jpg"
                ),
                "sub"
            )
        );
        when(userRepository.findByEmail("hong@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // When
        OAuth2User result = customOAuth2UserService.loadUser(request);

        // Then
        assertThat(result).isInstanceOf(OotdOAuth2UserDetails.class);
        OotdOAuth2UserDetails details = (OotdOAuth2UserDetails) result;
        assertThat(details.getUserDto().email()).isEqualTo("hong@gmail.com");
        assertThat(details.getUserDto().name()).isEqualTo("홍길동");

        verify(userRepository, times(1)).save(any(User.class));
        verify(oauthRepository, times(1)).save(any(OauthUser.class));
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    @DisplayName("구글 로그인 - 기존 사용자 로그인")
    void loadUser_Google_ExistingUser() {
        // Given
        OAuth2UserRequest request = createOAuth2UserRequest("google");
        User existingUser = new User("홍길동", "hong@gmail.com", null, Role.USER);
        ReflectionTestUtils.setField(existingUser, "id", UUID.randomUUID());
        UserDto userDto = new UserDto(existingUser.getId(), Instant.now(), "hong@gmail.com", "홍길동",Role.USER, List.of("google"), false);

        when(defaultOAuth2UserService.loadUser(any())).thenReturn(
            new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")), // 권한
                Map.of( // 유저 정보
                    "sub", "11223344556677889900", // 구글의 고유 ID 키
                    "name", "홍길동",
                    "email", "hong@gmail.com",
                    "picture", "https://lh3.googleusercontent.com/a/AEdFTp4.jpg"
                ),
                "sub"
            )
        );
        when(userRepository.findByEmail("hong@gmail.com")).thenReturn(Optional.of(existingUser));
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // When
        OAuth2User result = customOAuth2UserService.loadUser(request);

        // Then
        assertThat(result).isInstanceOf(OotdOAuth2UserDetails.class);
        OotdOAuth2UserDetails details = (OotdOAuth2UserDetails) result;
        assertThat(details.getUserDto().email()).isEqualTo("hong@gmail.com");

        verify(userRepository, never()).save(any(User.class));
        verify(oauthRepository, never()).save(any(OauthUser.class));
        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    @DisplayName("카카오 로그인 - 신규 사용자 생성")
    void loadUser_Kakao_NewUser() {
        // Given
        OAuth2UserRequest request = createOAuth2UserRequest("kakao");
        String expectedEmail = "카카오홍길동_12345@kakao.com";
        User newUser = new User("카카오홍길동", expectedEmail, null, Role.USER);
        ReflectionTestUtils.setField(newUser, "id", UUID.randomUUID());
        UserDto userDto = new UserDto(newUser.getId(), Instant.now(), "카카오홍길동_12345@kakao.com", "카카오홍길동",Role.USER, List.of("kakao"), false);


        when(defaultOAuth2UserService.loadUser(any())).thenReturn(
            new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")), // 권한
                Map.of( // 유저 정보
                    "id", "12345",
                    "kakao_account", Map.of(
                        "profile", Map.of("nickname", "카카오홍길동"),
                        "email", "카카오홍길동_12345@kakao.com"
                    )
                ),
                "id" // nameAttributeKey (기준 키)
            )
        );
        when(userRepository.findByEmail(expectedEmail)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // When
        OAuth2User result = customOAuth2UserService.loadUser(request);

        // Then
        assertThat(result).isInstanceOf(OotdOAuth2UserDetails.class);
        OotdOAuth2UserDetails details = (OotdOAuth2UserDetails) result;
        assertThat(details.getUserDto().email()).isEqualTo(expectedEmail);
        assertThat(details.getUserDto().name()).isEqualTo("카카오홍길동");

        verify(userRepository, times(1)).save(any(User.class));
        verify(oauthRepository, times(1)).save(any(OauthUser.class));
        verify(profileRepository, times(1)).save(any(Profile.class));
    }


    @Test
    @DisplayName("카카오 로그인 - 기존 사용자 로그인")
    void loadUser_Kakao_ExistingUser() {
        // Given
        OAuth2UserRequest request = createOAuth2UserRequest("kakao");
        User existingUser = new User("카카오홍길동", "카카오홍길동_12345@kakao.com", null, Role.USER);
        ReflectionTestUtils.setField(existingUser, "id", UUID.randomUUID());
        UserDto userDto = new UserDto(existingUser.getId(), Instant.now(), "카카오홍길동_12345@kakao.com", "카카오홍길동",Role.USER, List.of("google"), false);


        when(defaultOAuth2UserService.loadUser(any())).thenReturn(
            new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")), // 권한
                Map.of( // 유저 정보
                    "id", "12345",
                    "kakao_account", Map.of(
                        "profile", Map.of("nickname", "카카오홍길동"),
                        "email", "카카오홍길동_12345@kakao.com"
                    )
                ),
                "id" // nameAttributeKey (기준 키)
            )
        );
        when(userRepository.findByEmail("카카오홍길동_12345@kakao.com")).thenReturn(Optional.of(existingUser));
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // When
        OAuth2User result = customOAuth2UserService.loadUser(request);

        // Then
        assertThat(result).isInstanceOf(OotdOAuth2UserDetails.class);
        OotdOAuth2UserDetails details = (OotdOAuth2UserDetails) result;
        assertThat(details.getUserDto().email()).isEqualTo("카카오홍길동_12345@kakao.com");

        verify(userRepository, never()).save(any(User.class));
        verify(oauthRepository, never()).save(any(OauthUser.class));
        verify(profileRepository, never()).save(any(Profile.class));
    }

    private OAuth2UserRequest createOAuth2UserRequest(String registrationId) {
        ClientRegistration clientRegistration = ClientRegistration
            .withRegistrationId(registrationId)
            .clientId("test-client-id")
            .authorizationUri("https://test.com/oauth/authorize")
            .userNameAttributeName("id")
            .tokenUri("https://test.com/oauth/token")
            .userInfoUri("https://test.com/oauth/userinfo")
            .redirectUri("https://test.com/oauth/callback")
            .clientSecret("test-secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "test-token",
            null,
            null
        );

        return new OAuth2UserRequest(clientRegistration, accessToken);
    }
}