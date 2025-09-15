package com.sprint.ootd5team.clothes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sprint.ootd5team.base.storage.FileStorage;
import com.sprint.ootd5team.clothes.fixture.ClothesFixture;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothesAttributeRepository;
import com.sprint.ootd5team.domain.clothes.dto.request.ClothesCreateRequest;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothes.mapper.ClothesMapper;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import com.sprint.ootd5team.domain.clothes.service.ClothesServiceImpl;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClothesService 단위 테스트")
class ClothesServiceTest {

    @Mock
    private ClothesRepository clothesRepository;

    @Mock
    private ClothesMapper clothesMapper;

    @Mock
    private ClothesAttributeRepository clothesAttributeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorage fileStorage;

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
        ClothesDtoCursorResponse response = clothesService.getClothes(ownerId, null, null, null,
            10);

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
        given(clothesRepository.findClothes(eq(ownerId), eq(ClothesType.TOP), eq(null), eq(null),
            anyInt()))
            .willReturn(
                fakeClothes.stream()
                    .filter(c -> c.getType() == ClothesType.TOP)
                    .toList()
            );
        given(clothesMapper.toDto(any(Clothes.class))).willReturn(
            ClothesFixture.toDto(fakeClothes.get(0)));

        // when
        ClothesDtoCursorResponse response =
            clothesService.getClothes(ownerId, ClothesType.TOP, null, null, 10);

        // then
        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).name()).isEqualTo("흰 티셔츠");
    }

    @Test
    void 옷_목록_다음페이지가_존재하면_커서값을_반환한다() {
        // given
        List<Clothes> clothesList = IntStream.range(0, 11) // limit(10)보다 1개 더
            .mapToObj(i -> {
                Clothes c = Clothes.builder()
                    .owner(owner)
                    .name("옷" + i)
                    .type(ClothesType.TOP)
                    .imageUrl(null)
                    .build();
                String second = String.format("%02d", i);
                ReflectionTestUtils.setField(c, "createdAt", Instant.parse("2024-01-01T10:00:" + second + "Z"));
                ReflectionTestUtils.setField(c, "id", UUID.randomUUID());
                return c;
            })
            .toList();

        given(clothesRepository.findClothes(eq(ownerId), any(), any(), any(), anyInt()))
            .willReturn(clothesList);

        // when
        ClothesDtoCursorResponse response = clothesService.getClothes(ownerId, null, null, null, 10);

        // then
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isNotNull();
        assertThat(response.nextIdAfter()).isNotNull();
        assertThat(response.data()).hasSize(10);
    }

    @Test
    void 의상_생성_성공_이미지와_속성값_존재() {
        // given
        UUID attributeId = UUID.randomUUID();
        ClothesCreateRequest request = new ClothesCreateRequest(
            ownerId,
            "멋쟁이패딩",
            ClothesType.OUTER,
            List.of(
                new ClothesAttributeWithDefDto(attributeId, "계절",List.of("봄","여름","가을","겨울"), "겨울")
            )
        );
        MultipartFile mockImage = new MockMultipartFile(
            "image", "coat.png", "image/png", "fake-image".getBytes()
        );
        ClothesAttribute attribute = new ClothesAttribute("계절");
        ReflectionTestUtils.setField(attribute, "id", attributeId);
        Clothes clothes = Clothes.builder()
            .owner(owner)
            .name("멋쟁이패딩")
            .type(ClothesType.OUTER)
            .imageUrl("clothes/uuid_coat.png")
            .build();
        ClothesDto expectedDto = ClothesFixture.toDto(clothes);
        given(userRepository.findById(ownerId)).willReturn(Optional.of(owner));
        given(clothesAttributeRepository.findById(attributeId)).willReturn(Optional.of(attribute));
        given(fileStorage.upload(eq("coat.png"), any(InputStream.class)))
            .willReturn("clothes/uuid_coat.png");
        given(clothesMapper.toDto(any(Clothes.class))).willReturn(expectedDto);

        // when
        ClothesDto result = clothesService.create(request, mockImage);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("멋쟁이패딩");
        assertThat(result.type()).isEqualTo(ClothesType.OUTER);
        assertThat(result.imageUrl()).contains("clothes/uuid_coat.png");
        verify(clothesRepository).save(any(Clothes.class));
    }
}
