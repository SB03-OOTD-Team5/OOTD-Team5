package com.sprint.ootd5team.domain.recommendation.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import com.sprint.ootd5team.domain.recommendation.engine.model.ClothesScore;
import com.sprint.ootd5team.domain.recommendation.enums.ColorTone;
import com.sprint.ootd5team.domain.recommendation.enums.Material;
import com.sprint.ootd5team.domain.recommendation.enums.type.BottomType;
import com.sprint.ootd5team.domain.recommendation.enums.type.OuterType;
import com.sprint.ootd5team.domain.recommendation.enums.type.ShoesType;
import com.sprint.ootd5team.domain.recommendation.enums.type.TopType;
import com.sprint.ootd5team.domain.recommendation.fixture.RecommendationFixture;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SingleItemScoringEngine 단위 테스트")
class SingleItemScoringEngineTest {

    private int MAX_ITEMS_PER_TYPE = 5;

    @Test
    void 모든컴포넌트_null이면_기준점50() {
        // given
        SingleItemScoringEngine engine = new SingleItemScoringEngine();
        RecommendationInfoDto info = RecommendationFixture.defaultInfo();

        ClothesFilteredDto dto = mock(ClothesFilteredDto.class);
        given(dto.type()).willReturn(ClothesType.TOP);
        given(dto.name()).willReturn("테스트 상의");

        // when
        double score = engine.calculateSingleItemScore(info, dto);

        // then
        assertThat(score).isEqualTo(50.0);
    }

    @Test
    void 속성별_점수_모두적용() {
        // given
        SingleItemScoringEngine engine = new SingleItemScoringEngine();
        RecommendationInfoDto info = RecommendationFixture.defaultInfo();

        ClothesFilteredDto dto = mock(ClothesFilteredDto.class);
        given(dto.type()).willReturn(ClothesType.SHOES);
        given(dto.name()).willReturn("테스트 신발");

        ShoesType shoesType = mock(ShoesType.class);
        OuterType outerType = mock(OuterType.class);
        TopType topType = mock(TopType.class);
        BottomType bottomType = mock(BottomType.class);
        ColorTone colorTone = mock(ColorTone.class);
        Material material = mock(Material.class);

        given(shoesType.getWeatherScore(info)).willReturn(5.0);
        given(outerType.getWeatherScore(info)).willReturn(4.0);
        given(topType.getWeatherScore(info)).willReturn(3.0);
        given(bottomType.getWeatherScore(info)).willReturn(2.0);
        given(material.getWeatherScore(info)).willReturn(2.0);

        given(dto.shoesType()).willReturn(shoesType);
        given(dto.outerType()).willReturn(outerType);
        given(dto.topType()).willReturn(topType);
        given(dto.bottomType()).willReturn(bottomType);
        given(dto.colorTone()).willReturn(colorTone);
        given(dto.material()).willReturn(material);

        // when
        double score = engine.calculateSingleItemScore(info, dto);

        // then
        assertThat(score).isGreaterThan(50.0);
        assertThat(score).isLessThanOrEqualTo(60.0);
    }

    @Test
    void 점수가_너무낮을때_하한보정_적용() {
        // given
        SingleItemScoringEngine engine = new SingleItemScoringEngine();
        RecommendationInfoDto info = RecommendationFixture.coldInfo();

        ClothesFilteredDto dto = mock(ClothesFilteredDto.class);
        given(dto.type()).willReturn(ClothesType.OUTER);
        given(dto.name()).willReturn("테스트 아우터");

        OuterType outerType = mock(OuterType.class);
        given(outerType.getWeatherScore(info)).willReturn(-30.0);
        given(dto.outerType()).willReturn(outerType);

        // when
        double score = engine.calculateSingleItemScore(info, dto);

        // then
        assertThat(score).isEqualTo(45.0);
    }

    @Test
    void 점수가_너무높을때_상한보정_적용() {
        // given
        SingleItemScoringEngine engine = new SingleItemScoringEngine();
        RecommendationInfoDto info = RecommendationFixture.hotInfo();

        ClothesFilteredDto dto = mock(ClothesFilteredDto.class);
        given(dto.type()).willReturn(ClothesType.TOP);
        given(dto.name()).willReturn("테스트 상의");

        var topType = mock(TopType.class);
        given(topType.getWeatherScore(info)).willReturn(100.0);
        given(dto.topType()).willReturn(topType);

        // when
        double score = engine.calculateSingleItemScore(info, dto);

        // then
        assertThat(score).isEqualTo(60.0);
    }

    @Test
    void 타입별_그룹화_후_상위N개_반환() {
        // given
        SingleItemScoringEngine engine = spy(new SingleItemScoringEngine());
        RecommendationInfoDto info = RecommendationFixture.defaultInfo();

        List<ClothesFilteredDto> candidates = new ArrayList<>();

        // TOP 6개 (점수 10~60)
        for (int i = 1; i <= 6; i++) {
            ClothesFilteredDto dto = mock(ClothesFilteredDto.class);
            given(dto.type()).willReturn(ClothesType.TOP);
            given(dto.name()).willReturn("TOP-" + i);
            candidates.add(dto);
            willReturn(i * 10.0).given(engine).calculateSingleItemScore(info, dto);
        }

        // when
        List<ClothesScore> result = engine.getTopItemsByType(info, candidates);

        // then
        assertThat(result).hasSize(MAX_ITEMS_PER_TYPE);
        assertThat(result.get(0).score()).isEqualTo(60.0);
    }
}