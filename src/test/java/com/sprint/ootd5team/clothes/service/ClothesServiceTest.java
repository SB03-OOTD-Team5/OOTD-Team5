package com.sprint.ootd5team.clothes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.sprint.ootd5team.clothes.fixture.ClothesFixture;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothes.mapper.ClothesMapper;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import com.sprint.ootd5team.domain.clothes.service.ClothesServiceImpl;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClothesService 단위 테스트")
class ClothesServiceTest {

    @Mock
    private ClothesRepository clothesRepository;

    @Mock
    private ClothesMapper clothesMapper;

    @InjectMocks
    private ClothesServiceImpl clothesService;

    private final UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User();
        ReflectionTestUtils.setField(owner, "id", ownerId);
    }

    @Test
    void 옷_전체목록조회() {
        // given
        List<Clothes> fakeClothes = ClothesFixture.createTestClothes(owner);
        given(clothesRepository.findClothes(eq(ownerId), eq(null), eq(null), eq(null), anyInt()))
            .willReturn(fakeClothes);
        given(clothesMapper.toDto(any(Clothes.class)))
            .willAnswer(invocation -> {
                Clothes c = invocation.getArgument(0);
                return ClothesFixture.toDto(c);
            });

        // when
        ClothesDtoCursorResponse response = clothesService.getClothes(ownerId, null, null, null, 10);

        // then
        assertThat(response).isNotNull();
        assertThat(response.data()).hasSize(3);
        assertThat(response.data().get(0).name()).isEqualTo("흰 티셔츠");
        assertThat(response.data().get(1).name()).isEqualTo("청바지");
        assertThat(response.data().get(2).name()).isEqualTo("운동화");
        assertThat(response.hasNext()).isFalse();
        assertThat(response.totalCount()).isEqualTo(3L);
    }

    @Test
    void 옷_타입별조회_TOP() {
        // given
        List<Clothes> fakeClothes = ClothesFixture.createTestClothes(owner);
        given(clothesRepository.findClothes(eq(ownerId), eq(ClothesType.TOP), eq(null), eq(null), anyInt()))
            .willReturn(
                fakeClothes.stream()
                    .filter(c -> c.getType() == ClothesType.TOP)
                    .toList()
            );
        given(clothesMapper.toDto(any(Clothes.class))).willReturn(ClothesFixture.toDto(fakeClothes.get(0)));

        // when
        ClothesDtoCursorResponse response =
            clothesService.getClothes(ownerId, ClothesType.TOP, null, null, 10);

        // then
        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).name()).isEqualTo("흰 티셔츠");
    }
}
