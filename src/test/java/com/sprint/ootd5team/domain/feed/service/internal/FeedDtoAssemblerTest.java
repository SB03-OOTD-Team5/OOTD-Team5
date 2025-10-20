package com.sprint.ootd5team.domain.feed.service.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sprint.ootd5team.base.storage.FileStorage;
import com.sprint.ootd5team.domain.clothesattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.feed.dto.data.FeedDto;
import com.sprint.ootd5team.domain.feed.dto.data.OotdDto;
import com.sprint.ootd5team.domain.feed.repository.feedClothes.FeedClothesRepository;
import com.sprint.ootd5team.domain.user.dto.AuthorDto;
import com.sprint.ootd5team.domain.weather.dto.data.PrecipitationDto;
import com.sprint.ootd5team.domain.weather.dto.data.TemperatureDto;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherSummaryDto;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedDtoAssembler 슬라이스 테스트")
public class FeedDtoAssemblerTest {

    @Mock
    private FeedClothesRepository feedClothesRepository;

    @Mock
    private FileStorage fileStorage;

    @InjectMocks
    private FeedDtoAssembler assembler;

    private UUID feedId;
    private FeedDto baseDto;
    private OotdDto ootdDto;

    @BeforeEach
    void setUp() {
        feedId = UUID.randomUUID();

        AuthorDto author = new AuthorDto(UUID.randomUUID(), "nickname", "author/profile.png");
        WeatherSummaryDto weather = new WeatherSummaryDto(
            UUID.randomUUID(), SkyStatus.CLEAR,
            new PrecipitationDto(PrecipitationType.NONE, 0.0, 0.0),
            new TemperatureDto(25.0, 0.0, 20.0, 28.0)
        );

        baseDto = new FeedDto(
            feedId, Instant.now(), Instant.now(), author, weather,
            List.of(), "테스트 피드", 3L, 0L, false
        );

        ootdDto = new OotdDto(
            UUID.randomUUID(), "아디다스 반팔티", "ootd/image.png", "상의",
            List.of(new ClothesAttributeWithDefDto(
                UUID.randomUUID(), "색상", List.of("빨강", "파랑", "초록"), "빨강"
            ))
        );
    }

    @Test
    @DisplayName("FeedDto에 OOTD와 이미지 URL이 정상적으로 주입됨")
    void enrich_success() {
        // given
        given(feedClothesRepository.findOotdsByFeedIds(List.of(feedId)))
            .willReturn(Map.of(feedId, List.of(ootdDto)));

        given(fileStorage.resolveUrl("ootd/image.png"))
            .willReturn("resolved/ootd/image.png");
        given(fileStorage.resolveUrl("author/profile.png"))
            .willReturn("resolved/author/profile.png");

        // when
        List<FeedDto> result = assembler.enrich(List.of(baseDto));

        // then
        FeedDto enriched = result.get(0);

        assertThat(enriched.ootds()).hasSize(1);
        assertThat(enriched.ootds().get(0).imageUrl()).isEqualTo("resolved/ootd/image.png");
        assertThat(enriched.author().profileImageUrl()).isEqualTo("resolved/author/profile.png");

        verify(fileStorage).resolveUrl("ootd/image.png");
        verify(fileStorage).resolveUrl("author/profile.png");
    }

    @Test
    @DisplayName("OOTD 데이터가 없을 경우에도 NPE 없이 정상 처리됨")
    void enrich_emptyOotd() {
        // given
        given(feedClothesRepository.findOotdsByFeedIds(List.of(feedId)))
            .willReturn(Map.of());
        given(fileStorage.resolveUrl("author/profile.png"))
            .willReturn("resolved/author/profile.png");

        // when
        List<FeedDto> result = assembler.enrich(List.of(baseDto));

        // then
        FeedDto enriched = result.get(0);
        assertThat(enriched.ootds()).isEmpty();
        assertThat(enriched.author().profileImageUrl()).isEqualTo("resolved/author/profile.png");
    }
}