package com.sprint.ootd5team.domain.location.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.domain.location.dto.data.ClientCoords;
import com.sprint.ootd5team.domain.location.dto.data.LocationWithProfileIds;
import com.sprint.ootd5team.domain.location.dto.data.WeatherAPILocationDto;
import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.location.exception.LocationKakaoFetchException;
import com.sprint.ootd5team.domain.location.mapper.LocationMapper;
import com.sprint.ootd5team.domain.location.repository.LocationRepository;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocationService 단위 테스트")
@ActiveProfiles("test")
public class LocationServiceTest {

    @Mock
    WebClient kakaoApiClient;

    @Mock
    LocationRepository locationRepository;

    @Mock
    LocationMapper locationMapper;

    @Mock
    ProfileRepository profileRepository;

    @InjectMocks
    LocationServiceImpl service;

    private UUID testUserId;
    private Profile testProfile;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = new User("testuser", "test@test.com", "password", Role.USER);
        ReflectionTestUtils.setField(testUser, "id", testUserId);
        testProfile = new Profile(testUser, "nickname", null, null, null, null, null);
    }

    @Test
    @DisplayName("이미 저장된 위치 데이터가 있으면 외부 호출 없이 데이터 반환")
    void fetchLocation_returnsExistingData_withoutExternalCall() {
        // given
        var lat = new BigDecimal("37.98765");
        var lon = new BigDecimal("127.123456");

        // toNumeric(8,4) 반올림 값
        var latRounded = new BigDecimal("37.9877");
        var lonRounded = new BigDecimal("127.1235");

        Location entity = Location.builder()
            .latitude(latRounded)
            .longitude(lonRounded)
            .locationNames("서울 중구")
            .build();

        WeatherAPILocationDto dto = new WeatherAPILocationDto(
            lat,
            lon,
            null,
            null,
            new String[]{"서울", "중구"},
            latRounded,
            lonRounded
        );

        given(profileRepository.findByUserId(testUserId)).willReturn(Optional.of(testProfile));
        given(locationRepository.findByLatitudeAndLongitude(eq(latRounded), eq(lonRounded)))
            .willReturn(Optional.of(entity));
        given(locationMapper.toDto(eq(entity), any(ClientCoords.class))).willReturn(dto);

        // when
        WeatherAPILocationDto result = service.fetchLocation(lat, lon, testUserId);

        // then
        assertThat(result).isSameAs(dto);
        assertThat(testProfile.getLocation()).isEqualTo(entity);
        verifyNoInteractions(kakaoApiClient);
    }

    @Test
    @DisplayName("저장된 데이터가 없으면 외부 API를 호출")
    void fetchLocation_callsExternalApi_whenDataNotExists() {
        // given
        var lat = new BigDecimal("37.5000");
        var lon = new BigDecimal("126.9000");

        given(profileRepository.findByUserId(testUserId)).willReturn(Optional.of(testProfile));
        given(locationRepository.findByLatitudeAndLongitude(any(), any()))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.fetchLocation(lat, lon, testUserId))
            .isInstanceOf(LocationKakaoFetchException.class);

        verify(kakaoApiClient, atLeastOnce()).get();
    }

    @Test
    @DisplayName("외부 호출 실패 시 LocationKakaoFetchException 발생")
    void fetchLocation_throwsException_whenExternalCallFails() {
        // given
        var lat = new BigDecimal("37.5000");
        var lon = new BigDecimal("126.9000");

        given(profileRepository.findByUserId(testUserId)).willReturn(Optional.of(testProfile));
        given(locationRepository.findByLatitudeAndLongitude(any(), any()))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.fetchLocation(lat, lon, testUserId))
            .isInstanceOf(LocationKakaoFetchException.class);
    }

    @Test
    @DisplayName("위경도가 NUMERIC(8,4)로 반올림되어 Repository에 전달")
    void fetchLocation_roundsCoordinates_toNumeric() {
        // given
        given(profileRepository.findByUserId(testUserId)).willReturn(Optional.of(testProfile));
        given(locationRepository.findByLatitudeAndLongitude(any(), any()))
            .willReturn(Optional.empty());

        // when
        var lon = new BigDecimal("127.123456");
        var lat = new BigDecimal("37.98765");
        assertThatThrownBy(() -> service.fetchLocation(lat, lon, testUserId))
            .isInstanceOf(LocationKakaoFetchException.class);

        // then
        ArgumentCaptor<BigDecimal> latCap = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<BigDecimal> lonCap = ArgumentCaptor.forClass(BigDecimal.class);
        verify(locationRepository).findByLatitudeAndLongitude(latCap.capture(), lonCap.capture());
        assertThat(latCap.getValue()).isEqualByComparingTo("37.9877");
        assertThat(lonCap.getValue()).isEqualByComparingTo("127.1235");
    }

    @Test
    @DisplayName("존재하지 않는 userId로 호출시 ProfileNotFoundException 발생")
    void fetchLocation_userNotFound_throwsException() {
        // given
        var lat = new BigDecimal("37.5000");
        var lon = new BigDecimal("126.9000");
        UUID nonExistentUserId = UUID.randomUUID();

        given(profileRepository.findByUserId(nonExistentUserId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.fetchLocation(lat, lon, nonExistentUserId))
            .isInstanceOf(ProfileNotFoundException.class);

        verify(locationRepository, org.mockito.Mockito.never())
            .findByLatitudeAndLongitude(any(), any());
    }

    @Test
    @DisplayName("getLocationNames는 위치 이름만을 반환")
    void getLocationNames_returnsLocationName() {
        // given
        var lat = new BigDecimal("37.5000");
        var lon = new BigDecimal("126.9000");
        var latRounded = new BigDecimal("37.5000");
        var lonRounded = new BigDecimal("126.9000");

        Location entity = Location.builder()
            .latitude(latRounded)
            .longitude(lonRounded)
            .locationNames("서울특별시 중구 명동")
            .build();

        given(locationRepository.findByLatitudeAndLongitude(eq(latRounded), eq(lonRounded)))
            .willReturn(Optional.of(entity));

        // when
        String result = service.getLocationNames(lat, lon);

        // then
        assertThat(result).isEqualTo("서울특별시 중구 명동");
    }

    @Test
    @DisplayName("findOrCreateLocation은 기존 데이터가 있으면 반환")
    void findOrCreateLocation_returnsExistingLocation() {
        // given
        var lat = new BigDecimal("37.5000");
        var lon = new BigDecimal("126.9000");

        Location existingLocation = Location.builder()
            .latitude(lat)
            .longitude(lon)
            .locationNames("서울 중구")
            .build();

        given(locationRepository.findByLatitudeAndLongitude(any(), any()))
            .willReturn(Optional.of(existingLocation));

        // when
        Location result = service.findOrCreateLocation(lat, lon);

        // then
        assertThat(result).isSameAs(existingLocation);
        verify(locationRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    @DisplayName("findOrCreateLocation은 데이터가 없으면 Kakao API 호출")
    void findOrCreateLocation_callsKakaoApi_whenDataNotExists() {
        // given
        var lat = new BigDecimal("37.5665");
        var lon = new BigDecimal("126.9780");

        given(locationRepository.findByLatitudeAndLongitude(any(), any()))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.findOrCreateLocation(lat, lon))
            .isInstanceOf(LocationKakaoFetchException.class);

        verify(kakaoApiClient).get();
    }

    @Test
    @DisplayName("Kakao API 성공 응답 시 새 위치 생성 및 저장")
    void findOrCreateLocation_createsLocation_onSuccessResponse() {
        // given
        var lat = new BigDecimal("37.5665");
        var lon = new BigDecimal("126.9780");

        given(locationRepository.findByLatitudeAndLongitude(any(), any()))
            .willReturn(Optional.empty());

        // WebClient Mock
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        given(kakaoApiClient.get()).willReturn(uriSpec);
        given(uriSpec.uri(any(java.util.function.Function.class))).willReturn(uriSpec);

        String successResponse = "{\"meta\":{\"total_count\":1},\"documents\":[{\"region_type\":\"H\",\"address_name\":\"서울특별시 중구 명동\"}]}";
        given(uriSpec.exchangeToMono(any())).willReturn(reactor.core.publisher.Mono.just(successResponse));

        Location savedLocation = Location.builder()
            .latitude(new BigDecimal("37.5665"))
            .longitude(new BigDecimal("126.9780"))
            .locationNames("서울특별시 중구 명동")
            .build();
        given(locationRepository.save(any(Location.class))).willReturn(savedLocation);

        // when
        Location result = service.findOrCreateLocation(lat, lon);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getLocationNames()).isEqualTo("서울특별시 중구 명동");
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    @DisplayName("Kakao API 에러 응답 시 예외 발생")
    void findOrCreateLocation_throwsException_onErrorResponse() {
        // given
        var lat = new BigDecimal("37.5665");
        var lon = new BigDecimal("126.9780");

        given(locationRepository.findByLatitudeAndLongitude(any(), any()))
            .willReturn(Optional.empty());

        // WebClient Mock
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        given(kakaoApiClient.get()).willReturn(uriSpec);
        given(uriSpec.uri(any(java.util.function.Function.class))).willReturn(uriSpec);

        String errorResponse = "{\"code\":\"400\",\"msg\":\"Invalid request\"}";
        given(uriSpec.exchangeToMono(any())).willReturn(reactor.core.publisher.Mono.just(errorResponse));

        // when & then
        assertThatThrownBy(() -> service.findOrCreateLocation(lat, lon))
            .isInstanceOf(LocationKakaoFetchException.class);

        verify(locationRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    @DisplayName("행정동(H) 데이터가 없으면 예외 발생")
    void findOrCreateLocation_throwsException_whenNoHTypeData() {
        // given
        var lat = new BigDecimal("37.5665");
        var lon = new BigDecimal("126.9780");

        given(locationRepository.findByLatitudeAndLongitude(any(), any()))
            .willReturn(Optional.empty());

        // WebClient Mock
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        given(kakaoApiClient.get()).willReturn(uriSpec);
        given(uriSpec.uri(any(java.util.function.Function.class))).willReturn(uriSpec);

        String responseWithoutH = "{\"meta\":{\"total_count\":1},\"documents\":[{\"region_type\":\"B\",\"address_name\":\"서울특별시\"}]}";
        given(uriSpec.exchangeToMono(any())).willReturn(reactor.core.publisher.Mono.just(responseWithoutH));

        // when & then
        assertThatThrownBy(() -> service.findOrCreateLocation(lat, lon))
            .isInstanceOf(LocationKakaoFetchException.class)
            .hasMessageContaining("해당하는 행정동 데이터가 없습니다");

        verify(locationRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    @DisplayName("Kakao API 호출 실패 시 LocationKakaoFetchException 발생")
    void findOrCreateLocation_throwsException_onApiFailure() {
        // given
        var lat = new BigDecimal("37.5665");
        var lon = new BigDecimal("126.9780");

        given(locationRepository.findByLatitudeAndLongitude(any(), any()))
            .willReturn(Optional.empty());
        given(kakaoApiClient.get()).willThrow(new RuntimeException("Network error"));

        // when & then
        assertThatThrownBy(() -> service.findOrCreateLocation(lat, lon))
            .isInstanceOf(LocationKakaoFetchException.class);

        verify(locationRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    @DisplayName("Profile 리스트가 비어있으면 빈 리스트를 반환")
    void findAllLocationUsingInProfileDistinct_returnsEmpty_whenNoProfiles() {
        // given
        given(profileRepository.findAllByLocationIsNotNull())
            .willReturn(List.of());

        // when
        List<LocationWithProfileIds> result = service.findAllLocationUsingInProfileDistinct();

        // then
        assertThat(result).isEmpty();
    }
}