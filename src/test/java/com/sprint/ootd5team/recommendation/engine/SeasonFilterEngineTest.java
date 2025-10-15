package com.sprint.ootd5team.recommendation.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttributeValue;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import com.sprint.ootd5team.domain.recommendation.engine.SeasonFilterEngine;
import com.sprint.ootd5team.domain.recommendation.mapper.RecommendationMapper;
import com.sprint.ootd5team.recommendation.fixture.RecommendationFixture;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SeasonFilterEngine 단위 테스트")
class SeasonFilterEngineTest {

    @Mock
    ClothesRepository clothesRepository;

    @Mock
    RecommendationMapper recommendationMapper;

    @Test
    @DisplayName("가을 기준 계절 속성 필터링 동작 확인")
    void 가을기준_계절속성_필터링() {
        // given
        SeasonFilterEngine engine = new SeasonFilterEngine(clothesRepository, recommendationMapper);
        UUID userId = UUID.randomUUID();
        RecommendationInfoDto info = RecommendationFixture.coldInfo();

        ClothesAttribute seasonAttr = mock(ClothesAttribute.class);
        given(seasonAttr.getName()).willReturn("계절");

        // 봄/가을
        ClothesAttributeValue cavSpringAutumn = mock(ClothesAttributeValue.class);
        given(cavSpringAutumn.getAttribute()).willReturn(seasonAttr);
        given(cavSpringAutumn.getDefValue()).willReturn("봄/가을");

        Clothes springAutumn = mock(Clothes.class);
        given(springAutumn.getName()).willReturn("봄가을 아이템");
        given(springAutumn.getClothesAttributeValues()).willReturn(List.of(cavSpringAutumn));

        // 가을
        ClothesAttributeValue cavAutumn = mock(ClothesAttributeValue.class);
        given(cavAutumn.getAttribute()).willReturn(seasonAttr);
        given(cavAutumn.getDefValue()).willReturn("가을");

        Clothes autumnOnly = mock(Clothes.class);
        given(autumnOnly.getName()).willReturn("가을 아이템");
        given(autumnOnly.getClothesAttributeValues()).willReturn(List.of(cavAutumn));

        // 여름 (제외 대상)
        ClothesAttributeValue cavSummer = mock(ClothesAttributeValue.class);
        given(cavSummer.getAttribute()).willReturn(seasonAttr);
        given(cavSummer.getDefValue()).willReturn("여름");

        Clothes summerOnly = mock(Clothes.class);
        given(summerOnly.getName()).willReturn("여름 아이템");
        given(summerOnly.getClothesAttributeValues()).willReturn(List.of(cavSummer));

        // 공백 (제외 대상)
        ClothesAttributeValue cavEmpty = mock(ClothesAttributeValue.class);
        given(cavEmpty.getAttribute()).willReturn(seasonAttr);
        given(cavEmpty.getDefValue()).willReturn("");

        Clothes noSeason = mock(Clothes.class);
        given(noSeason.getName()).willReturn("무계절 아이템");
        given(noSeason.getClothesAttributeValues()).willReturn(List.of(cavEmpty));

        given(clothesRepository.findByOwnerWithSeasonAttribute(userId))
            .willReturn(List.of(springAutumn, autumnOnly, summerOnly, noSeason));

        given(recommendationMapper.toFilteredDto(any()))
            .willReturn(mock(ClothesFilteredDto.class));

        // when
        List<ClothesFilteredDto> result = engine.getFilteredClothes(userId, info);

        // then
        assertThat(result).hasSize(2);
        verify(recommendationMapper, times(2)).toFilteredDto(any());
    }
}