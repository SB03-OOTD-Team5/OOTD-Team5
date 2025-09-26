//package com.sprint.ootd5team.location.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.atLeastOnce;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoInteractions;
//
//import com.sprint.ootd5team.domain.location.dto.data.WeatherAPILocationDto;
//import com.sprint.ootd5team.domain.location.entity.Location;
//import com.sprint.ootd5team.domain.location.exception.LocationKakaoFetchException;
//import com.sprint.ootd5team.domain.location.mapper.LocationMapper;
//import com.sprint.ootd5team.domain.location.repository.LocationRepository;
//import com.sprint.ootd5team.domain.location.service.LocationServiceImpl;
//import java.math.BigDecimal;
//import java.util.Optional;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.web.reactive.function.client.WebClient;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("LocationService 단위 테스트")
//@ActiveProfiles("test")
//public class LocationServiceTest {
//
//    @Mock
//    WebClient kakaoApiClient;
//    @Mock
//    LocationRepository locationRepository;
//    @Mock
//    LocationMapper locationMapper;
//
//    @InjectMocks
//    LocationServiceImpl service;
//
//    @Test
//    @DisplayName("이미 저장된 위치 데이터가 있으면 외부 호출 없이 데이터를 반환한다")
//    void 데이터가_이미_존재하면_외부_API_호출하지_않는다() {
//        // given
//        var lat = new BigDecimal("37.98765");
//        var lon = new BigDecimal("127.123456");
//
//        // toNumeric(8,4) 반올림 값
//        var latRounded = new BigDecimal("37.9877");
//        var lonRounded = new BigDecimal("127.1235");
//
//        Location entity = mock(Location.class);
//        WeatherAPILocationDto dto = new WeatherAPILocationDto(latRounded, lonRounded, null, null,
//            new String[]{"서울", "중구"});
//
//        given(locationRepository.findByLatitudeAndLongitude(eq(latRounded), eq(lonRounded)))
//            .willReturn(Optional.of(entity));
//        given(locationMapper.toDto(entity)).willReturn(dto);
//
//        // when
//        WeatherAPILocationDto result = service.fetchLocation(lat, lon);
//
//        // then
//        assertThat(result).isSameAs(dto);
//        verifyNoInteractions(kakaoApiClient);
//    }
//
//    @Test
//    @DisplayName("저장된 데이터가 없으면 외부 API를 호출한다.")
//    void 미존재시_외부API_호출() {
//        // given
//        var lat = new BigDecimal("37.5000");
//        var lon = new BigDecimal("126.9000");
//
//        given(locationRepository.findByLatitudeAndLongitude(any(), any()))
//            .willReturn(Optional.empty());
//
//        // when
//        // 서비스 실제 호출 -> 예외 던짐
//        assertThatThrownBy(() -> service.fetchLocation(lat, lon))
//            .isInstanceOf(LocationKakaoFetchException.class);
//
//        // then
//        // 외부호출있음(예외 던진것도 반영됨)
//        verify(kakaoApiClient, atLeastOnce()).get();
//    }
//
//    @Test
//    @DisplayName("외부 호출 실패 시 LocationKakaoFetchException을 던진다")
//    void fetch_throws_whenExternalCallFails() {
//        // given
//        var lat = new BigDecimal("37.5000");
//        var lon = new BigDecimal("126.9000");
//
//        given(locationRepository.findByLatitudeAndLongitude(any(), any()))
//            .willReturn(Optional.empty());
//
//        // when & then
//        // 서비스 실제 호출 -> 예외 던짐
//        assertThatThrownBy(() -> service.fetchLocation(lat, lon))
//            .isInstanceOf(LocationKakaoFetchException.class);
//
//    }
//
//    @Test
//    @DisplayName("위경도가 NUMERIC(8,4)로 반올림되어 Repository에 전달된다")
//    void toNumeric_반올림_레포지토리인자검증() {
//        // given
//        given(locationRepository.findByLatitudeAndLongitude(any(), any()))
//            .willReturn(Optional.empty());
//
//        // when
//        var lon = new BigDecimal("127.123456");
//        var lat = new BigDecimal("37.98765");
//        assertThatThrownBy(() -> service.fetchLocation(lat, lon))
//            .isInstanceOf(LocationKakaoFetchException.class);
//
//        // then: 레포지토리에 전달된 반올림 값 캡쳐
//        ArgumentCaptor<BigDecimal> latCap = ArgumentCaptor.forClass(BigDecimal.class);
//        ArgumentCaptor<BigDecimal> lonCap = ArgumentCaptor.forClass(BigDecimal.class);
//        verify(locationRepository).findByLatitudeAndLongitude(latCap.capture(), lonCap.capture());
//        assertThat(latCap.getValue()).isEqualByComparingTo("37.9877");
//        assertThat(lonCap.getValue()).isEqualByComparingTo("127.1235");
//    }
//
//}
