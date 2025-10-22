package com.sprint.ootd5team.domain.recommendation.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttributeValue;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import com.sprint.ootd5team.domain.recommendation.mapper.RecommendationMapper;
import com.sprint.ootd5team.domain.recommendation.fixture.RecommendationFixture;
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
    void 가을기준_계절속성_필터링() {
        // given
        SeasonFilterEngine engine = new SeasonFilterEngine(clothesRepository, recommendationMapper);
        UUID userId = UUID.randomUUID();
        RecommendationInfoDto info = RecommendationFixture.coldInfo();

        // ===== Mock Clothes 생성 =====
        ClothesAttribute seasonAttr = mock(ClothesAttribute.class);
        given(seasonAttr.getName()).willReturn("계절");

        // 봄/가을 의상
        ClothesAttributeValue cavSpringAutumn = mock(ClothesAttributeValue.class);
        given(cavSpringAutumn.getAttribute()).willReturn(seasonAttr);
        given(cavSpringAutumn.getDefValue()).willReturn("봄/가을");

        Clothes springAutumn = mock(Clothes.class);
        given(springAutumn.getId()).willReturn(UUID.randomUUID());
        given(springAutumn.getName()).willReturn("봄가을 아이템");
        given(springAutumn.getType()).willReturn(ClothesType.TOP);
        given(springAutumn.getClothesAttributeValues()).willReturn(List.of(cavSpringAutumn));

        // 가을 의상
        ClothesAttributeValue cavAutumn = mock(ClothesAttributeValue.class);
        given(cavAutumn.getAttribute()).willReturn(seasonAttr);
        given(cavAutumn.getDefValue()).willReturn("가을");

        Clothes autumnOnly = mock(Clothes.class);
        given(autumnOnly.getId()).willReturn(UUID.randomUUID());
        given(autumnOnly.getName()).willReturn("가을 아이템");
        given(autumnOnly.getType()).willReturn(ClothesType.OUTER);
        given(autumnOnly.getClothesAttributeValues()).willReturn(List.of(cavAutumn));

        // 여름 (제외 대상)
        ClothesAttributeValue cavSummer = mock(ClothesAttributeValue.class);
        given(cavSummer.getAttribute()).willReturn(seasonAttr);
        given(cavSummer.getDefValue()).willReturn("여름");

        Clothes summerOnly = mock(Clothes.class);
        given(summerOnly.getId()).willReturn(UUID.randomUUID());
        given(summerOnly.getName()).willReturn("여름 아이템");
        given(summerOnly.getType()).willReturn(ClothesType.BOTTOM);
        given(summerOnly.getClothesAttributeValues()).willReturn(List.of(cavSummer));

        List<UUID> filteredIds = List.of(springAutumn.getId(), autumnOnly.getId());
        List<Clothes> clothesList = List.of(springAutumn, autumnOnly);
        given(clothesRepository.findClothesIdsBySeasonFilter(eq(userId), any(String[].class), eq(true)))
            .willReturn(filteredIds);
        given(clothesRepository.findAllWithAttributesByIds(filteredIds))
            .willReturn(clothesList);
        ClothesFilteredDto dto1 = mock(ClothesFilteredDto.class);
        ClothesFilteredDto dto2 = mock(ClothesFilteredDto.class);
        given(recommendationMapper.toFilteredDto(any())).willReturn(dto1, dto2);

        // when
        List<ClothesFilteredDto> result = engine.getFilteredClothes(userId, info);

        // then
        assertThat(result)
            .hasSize(2);

        verify(clothesRepository, times(1))
            .findClothesIdsBySeasonFilter(eq(userId), any(String[].class), eq(true));
        verify(clothesRepository, times(1))
            .findAllWithAttributesByIds(anyList());
        verify(recommendationMapper, times(2))
            .toFilteredDto(any());
    }
}