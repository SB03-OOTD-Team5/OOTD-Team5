package com.sprint.ootd5team.recommendation.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.recommendation.engine.OutfitCombinationGenerator;
import com.sprint.ootd5team.domain.recommendation.engine.model.ClothesScore;
import com.sprint.ootd5team.domain.recommendation.engine.model.OutfitScore;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.enums.ClothesStyle;
import com.sprint.ootd5team.domain.recommendation.enums.ColorTone;
import com.sprint.ootd5team.domain.recommendation.enums.Material;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OutfitCombinationGenerator 단위 테스트")
class OutfitCombinationGeneratorEngineTest {

    @Test
    void 상하기본조합_생성_선택아이템_확장() {
        // given
        OutfitCombinationGenerator generator = new OutfitCombinationGenerator();

        List<ClothesScore> candidates = new ArrayList<>();

        // TOP 2
        candidates.add(mockScore("TOP-1", ClothesType.TOP, 70));
        candidates.add(mockScore("TOP-2", ClothesType.TOP, 65));

        // BOTTOM 2
        candidates.add(mockScore("BOTTOM-1", ClothesType.BOTTOM, 60));
        candidates.add(mockScore("BOTTOM-2", ClothesType.BOTTOM, 55));

        // OUTER 1 (선택 확장)
        candidates.add(mockScore("OUTER-1", ClothesType.OUTER, 62));

        // when
        List<OutfitScore> result = generator.generateWithScoring(candidates);

        // then
        assertThat(result).isNotEmpty();
        boolean hasTopBottom = result.stream().anyMatch(o ->
            o.getItems().stream().anyMatch(i -> i.item().type() == ClothesType.TOP) &&
            o.getItems().stream().anyMatch(i -> i.item().type() == ClothesType.BOTTOM)
        );
        assertThat(hasTopBottom).isTrue();
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
        return new ClothesScore(
            dto,
            score,
            ColorTone.NEUTRAL,
            Material.COTTON,
            ClothesStyle.CASUAL
        );
    }
}