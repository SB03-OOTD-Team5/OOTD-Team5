package com.sprint.ootd5team.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothes.fixture.ClothesFixture;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.mapper.RecommendationMapper;
import com.sprint.ootd5team.domain.recommendation.service.RecommendationFallbackService;
import com.sprint.ootd5team.domain.user.entity.User;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationFallbackService 단위 테스트")
class RecommendationFallbackServiceTest {

    @Mock
    private ClothesRepository clothesRepository;
    @Mock
    private RecommendationMapper recommendationMapper;

    @InjectMocks
    private RecommendationFallbackService fallbackService;

    private UUID userId = UUID.randomUUID();
    private User user;
    private List<Clothes> clothesList;


    @BeforeEach
    void setup() {
        user = new User("쪼쪼", null, null, null);
        clothesList = ClothesFixture.createTestClothes(user);
    }

    @Test
    void 사용자의_옷이_없으면_빈리스트() {
        // given
        given(clothesRepository.findByOwner_Id(userId)).willReturn(List.of());

        // when
        List<ClothesFilteredDto> result = fallbackService.getRandomOutfit(userId);

        // then
        assertThat(result).isEmpty();
        then(clothesRepository).should().findByOwner_Id(userId);
    }

    @Test
    void 상의_하의_신발_조합_생성() {
        // given
        given(clothesRepository.findByOwner_Id(userId)).willReturn(clothesList);
        given(recommendationMapper.toFilteredDto(any(Clothes.class)))
            .willAnswer(invocation -> {
                Clothes c = invocation.getArgument(0);
                return createDto(c);
            });


        // when
        List<ClothesFilteredDto> result = fallbackService.getRandomOutfit(userId);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.stream().map(ClothesFilteredDto::type))
            .contains(ClothesType.TOP, ClothesType.BOTTOM, ClothesType.SHOES);
        then(clothesRepository).should().findByOwner_Id(userId);
        then(recommendationMapper).should(times(3)).toFilteredDto(any(Clothes.class));
    }

    // === 헬퍼 메서드 ===
    private ClothesFilteredDto createDto(Clothes c) {
        return new ClothesFilteredDto(
            UUID.randomUUID(),
            c.getName(),
            null,
            c.getType(),
            List.of()
        );
    }
}
