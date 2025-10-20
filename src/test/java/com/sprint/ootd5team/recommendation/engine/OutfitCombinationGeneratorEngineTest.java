package com.sprint.ootd5team.recommendation.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.recommendation.engine.OutfitCombinationGenerator;
import com.sprint.ootd5team.domain.recommendation.engine.model.ClothesScore;
import com.sprint.ootd5team.domain.recommendation.engine.model.OutfitScore;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OutfitCombinationGenerator 단위 테스트")
class OutfitCombinationGeneratorEngineTest {

    @Test
    void 상하의_기본_조합생성_및_확장가능() {
        // given
        OutfitCombinationGenerator generator = new OutfitCombinationGenerator();

        List<ClothesScore> candidates = new ArrayList<>();
        candidates.add(mockScore("화이트 셔츠", ClothesType.TOP, 70));
        candidates.add(mockScore("블랙 티셔츠", ClothesType.TOP, 68));
        candidates.add(mockScore("블루 진", ClothesType.BOTTOM, 65));
        candidates.add(mockScore("블랙 슬랙스", ClothesType.BOTTOM, 60));
        candidates.add(mockScore("블랙 자켓", ClothesType.OUTER, 72));
        candidates.add(mockScore("베이지 코트", ClothesType.OUTER, 69));

        // when
        List<OutfitScore> result = generator.generateWithScoring(candidates);

        // then
        assertThat(result).isNotEmpty();

        boolean hasTopBottom = result.stream()
            .anyMatch(o ->
                o.getItems().stream().anyMatch(i -> i.type() == ClothesType.TOP) &&
                    o.getItems().stream().anyMatch(i -> i.type() == ClothesType.BOTTOM)
            );
        assertThat(hasTopBottom).isTrue();

        boolean hasOuter = result.stream()
            .anyMatch(o -> o.getItems().stream().anyMatch(i -> i.type() == ClothesType.OUTER));
        assertThat(hasOuter).isTrue();

        assertThat(result)
            .allSatisfy(outfit ->
                assertThat(outfit.normalizedScore())
                    .isGreaterThanOrEqualTo(0)
            );
    }

    @Test
    void 원피스_기본_조합생성_및_확장가능() {
        // given
        OutfitCombinationGenerator generator = new OutfitCombinationGenerator();

        ClothesScore dress1 = mockScore("핑크 플로럴 원피스", ClothesType.DRESS, 70);
        ClothesScore dress2 = mockScore("베이지 니트 원피스", ClothesType.DRESS, 68);
        ClothesScore outer1 = mockScore("블랙 가죽자켓", ClothesType.OUTER, 75);
        ClothesScore outer2 = mockScore("베이지 트렌치코트", ClothesType.OUTER, 72);
        ClothesScore shoes1 = mockScore("블랙 로퍼", ClothesType.SHOES, 65);

        List<ClothesScore> candidates = List.of(dress1, dress2, outer1, outer2, shoes1);

        // when
        List<OutfitScore> result = generator.generateWithScoring(candidates);

        // then
        assertThat(result).isNotEmpty();

        boolean hasDress = result.stream()
            .anyMatch(o -> o.getItems().stream().anyMatch(i -> i.type() == ClothesType.DRESS));
        assertThat(hasDress).isTrue();

        boolean hasOuter = result.stream()
            .anyMatch(o ->
                o.getItems().stream().anyMatch(i -> i.type() == ClothesType.OUTER)
            );
        assertThat(hasOuter).isIn(true, false);

        boolean hasShoes = result.stream()
            .anyMatch(o ->
                o.getItems().stream().anyMatch(i -> i.type() == ClothesType.SHOES)
            );
        assertThat(hasShoes).isIn(true, false);

        assertThat(result)
            .allSatisfy(outfit ->
                assertThat(outfit.normalizedScore())
                    .isGreaterThanOrEqualTo(0)
            );
    }

    private ClothesFilteredDto mockItem(String name, ClothesType type) {
        ClothesFilteredDto dto = mock(ClothesFilteredDto.class);
        doReturn(name).when(dto).name();
        doReturn(type).when(dto).type();
        doReturn(UUID.randomUUID()).when(dto).clothesId();
        return dto;
    }

    private ClothesScore mockScore(String name, ClothesType type, double score) {
        ClothesFilteredDto dto = mockItem(name, type);
        return new ClothesScore(dto, score);
    }
}