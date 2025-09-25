package com.sprint.ootd5team.domain.clothes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sprint.ootd5team.base.exception.clothes.ClothesNotFoundException;
import com.sprint.ootd5team.base.exception.clothes.ClothesSaveFailedException;
import com.sprint.ootd5team.base.exception.clothesattribute.AttributeNotFoundException;
import com.sprint.ootd5team.base.exception.clothesattribute.AttributeValueNotAllowedException;
import com.sprint.ootd5team.base.exception.file.FileSaveFailedException;
import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.base.storage.FileStorage;
import com.sprint.ootd5team.domain.clothes.fixture.ClothesFixture;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDto;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeValue;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothesAttributeRepository;
import com.sprint.ootd5team.domain.clothes.dto.request.ClothesCreateRequest;
import com.sprint.ootd5team.domain.clothes.dto.request.ClothesUpdateRequest;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothes.mapper.ClothesMapper;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.io.IOException;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClothesService 단위 테스트")
class ClothesServiceTest {

    private final UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
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
        given(clothesRepository.findByOwnerWithCursor(eq(ownerId), eq(null), eq(null), eq(null),
            anyInt(), eq(Direction.DESC)))
            .willReturn(fakeClothes);
        given(clothesMapper.toDto(any(Clothes.class)))
            .willAnswer(invocation -> {
                Clothes c = invocation.getArgument(0);
                return ClothesFixture.toDto(c);
            });
        given(clothesRepository.countByOwner_Id(ownerId)).willReturn(3L);

        // when
        ClothesDtoCursorResponse response = clothesService.getClothes(ownerId, null, null, null,
            10, Direction.DESC);

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
    void 옷_목록조회_limit초과시_hasNext_true() {
        // given
        Instant cursor = Instant.now();
        int limit = 2;

        List<Clothes> fakeClothes = ClothesFixture.createTestClothes(owner);
        given(clothesRepository.findByOwnerWithCursor(eq(ownerId), eq(null), eq(cursor), eq(null),
            anyInt(), eq(Direction.DESC)))
            .willReturn(fakeClothes);

        given(clothesMapper.toDto(any(Clothes.class)))
            .willAnswer(invocation -> {
                Clothes c = invocation.getArgument(0);
                return ClothesFixture.toDto(c);
            });

        // when
        ClothesDtoCursorResponse response =
            clothesService.getClothes(ownerId, null, cursor, null, limit, Direction.DESC);

        // then
        assertThat(response).isNotNull();
        assertThat(response.data()).hasSize(limit);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isNotNull();
        assertThat(response.nextIdAfter()).isNotNull();
    }

    @Test
    void 옷_타입별조회_TOP() {
        // given
        List<Clothes> fakeClothes = ClothesFixture.createTestClothes(owner);
        given(clothesRepository.findByOwnerWithCursor(eq(ownerId), eq(ClothesType.TOP), eq(null),
            eq(null),
            anyInt(), eq(Direction.DESC)))
            .willReturn(
                fakeClothes.stream()
                    .filter(c -> c.getType() == ClothesType.TOP)
                    .toList()
            );
        given(clothesMapper.toDto(any(Clothes.class))).willReturn(
            ClothesFixture.toDto(fakeClothes.get(0)));

        // when
        ClothesDtoCursorResponse response =
            clothesService.getClothes(ownerId, ClothesType.TOP, null, null, 10, Direction.DESC);

        // then
        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).name()).isEqualTo("흰 티셔츠");
    }

    @Test
    void 옷_목록_다음페이지가_존재하면_커서값을_반환한다() {
        // given
        List<Clothes> clothesList = IntStream.range(0, 11)
            .mapToObj(i -> {
                Clothes c = Clothes.builder()
                    .owner(owner)
                    .name("옷" + i)
                    .type(ClothesType.TOP)
                    .imageUrl(null)
                    .build();
                String second = String.format("%02d", i);
                ReflectionTestUtils.setField(c, "createdAt",
                    Instant.parse("2024-01-01T10:00:" + second + "Z"));
                ReflectionTestUtils.setField(c, "id", UUID.randomUUID());
                return c;
            })
            .toList();

        given(clothesRepository.findByOwnerWithCursor(eq(ownerId), any(), any(), any(), anyInt(),
            eq(Direction.DESC)))
            .willReturn(clothesList);

        // when
        ClothesDtoCursorResponse response = clothesService.getClothes(ownerId, null, null, null,
            10, Direction.DESC);

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
        UUID imageId = UUID.randomUUID();
        String imageUrl = "clothes/" + imageId;
        ClothesCreateRequest request = new ClothesCreateRequest(
            ownerId,
            "멋쟁이패딩",
            ClothesType.OUTER,
            List.of(
                new ClothesAttributeDto(attributeId, "겨울")
            )
        );
        MultipartFile mockImage = new MockMultipartFile(
            "image", "coat.png", "image/png", "fake-image".getBytes()
        );
        ClothesAttribute attribute = ClothesFixture.createSeasonAttribute(attributeId);
        Clothes clothes = Clothes.builder()
            .owner(owner)
            .name("멋쟁이패딩")
            .type(ClothesType.OUTER)
            .imageUrl(imageUrl)
            .build();
        ClothesDto expectedDto = ClothesFixture.toDto(clothes);
        given(userRepository.findById(ownerId)).willReturn(Optional.of(owner));
        given(clothesAttributeRepository.findById(attributeId)).willReturn(Optional.of(attribute));
        given(fileStorage.upload(any(), any(InputStream.class), eq("image/png"), any())).willReturn(
            imageUrl);
        given(clothesMapper.toDto(any(Clothes.class))).willReturn(expectedDto);

        // when
        ClothesDto result = clothesService.create(request, mockImage);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("멋쟁이패딩");
        assertThat(result.type()).isEqualTo(ClothesType.OUTER);
        assertThat(result.imageUrl()).isEqualTo(imageUrl);
        verify(fileStorage).upload(any(), any(InputStream.class), eq("image/png"), any());
    }

    @Test
    void 이미지_없이_의상을_생성할_수_있다() {
        // given
        ClothesCreateRequest request = new ClothesCreateRequest(ownerId, "멋진티셔츠", ClothesType.TOP,
            null);
        ClothesDto expected = new ClothesDto(UUID.randomUUID(), ownerId, "멋진티셔츠", null,
            ClothesType.TOP, List.of());

        given(userRepository.findById(ownerId)).willReturn(Optional.of(owner));
        given(clothesMapper.toDto(any(Clothes.class))).willReturn(expected);

        // when
        ClothesDto result = clothesService.create(request, null);

        // then
        assertThat(result.imageUrl()).isNull();
        verify(clothesRepository).save(any(Clothes.class));
        verify(fileStorage, never()).upload(any(), any(), any(), any());
    }

    @Test
    void 의상_생성시_허용되지않은_속성값이면_예외를_발생시킨다() {
        // given
        UUID attributeId = UUID.randomUUID();
        ClothesCreateRequest request = new ClothesCreateRequest(
            ownerId, "셔츠", ClothesType.TOP, List.of(new ClothesAttributeDto(attributeId, "한여름"))
        );

        ClothesAttribute attribute = ClothesFixture.createSeasonAttribute(attributeId);

        given(userRepository.findById(ownerId)).willReturn(Optional.of(owner));
        given(clothesAttributeRepository.findById(attributeId)).willReturn(Optional.of(attribute));

        // when & then
        assertThatThrownBy(() -> clothesService.create(request, null))
            .isInstanceOf(AttributeValueNotAllowedException.class);
    }


    @Test
    void 의상_생성시_속성값을_찾지못하면_예외를_던진다() {
        // given
        UUID attrId = UUID.randomUUID();
        ClothesCreateRequest request = new ClothesCreateRequest(
            ownerId, "셔츠", ClothesType.TOP, List.of(new ClothesAttributeDto(attrId, "겨울"))
        );

        given(userRepository.findById(ownerId)).willReturn(Optional.of(owner));
        given(clothesAttributeRepository.findById(attrId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clothesService.create(request, null))
            .isInstanceOf(AttributeNotFoundException.class);
    }

    @Test
    void 의상_생성시_존재하지않는_사용자면_예외를_던진다() {
        // given
        ClothesCreateRequest request = new ClothesCreateRequest(
            ownerId, "셔츠", ClothesType.TOP, null
        );
        given(userRepository.findById(ownerId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clothesService.create(request, null))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void 의상_생성_중_DB저장_실패시_ClothesSaveFailedException() {
        // given
        ClothesCreateRequest request = new ClothesCreateRequest(
            ownerId,
            "DB실패셔츠",
            ClothesType.TOP,
            null
        );
        MultipartFile mockImage = new MockMultipartFile(
            "image", "fail.png", "image/png", "fake".getBytes()
        );
        given(userRepository.findById(ownerId)).willReturn(Optional.of(owner));
        given(fileStorage.upload(eq("fail.png"), any(InputStream.class), eq("image/png"), any()))
            .willReturn("clothes/fail.png");
        given(clothesRepository.save(any(Clothes.class)))
            .willThrow(new RuntimeException("DB error"));

        // when & then
        assertThatThrownBy(() -> clothesService.create(request, mockImage))
            .isInstanceOf(ClothesSaveFailedException.class);
        verify(fileStorage).delete("clothes/fail.png");
    }

    @Test
    void 의상_생성시_InputStream실패_FileSaveFailedException_발생() throws Exception {
        // given
        ClothesCreateRequest request = new ClothesCreateRequest(
            ownerId,
            "고장난셔츠",
            ClothesType.TOP,
            null
        );
        MultipartFile mockImage = Mockito.mock(MultipartFile.class);
        given(mockImage.getOriginalFilename()).willReturn("broken.png");
        given(mockImage.getInputStream()).willThrow(new IOException("파일 열기 실패"));

        // User 는 정상 조회
        given(userRepository.findById(ownerId)).willReturn(Optional.of(owner));

        // when & then
        assertThatThrownBy(() -> clothesService.create(request, mockImage))
            .isInstanceOf(FileSaveFailedException.class);
        verify(fileStorage, never()).upload(any(), any(), any(), any());
        verify(fileStorage, never()).delete(any());
    }

    @Test
    void 의상_수정_성공_이름과_타입변경() {
        // given
        UUID clothesId = UUID.randomUUID();
        Clothes clothes = ClothesFixture.createClothesEntity(owner, "구티셔츠", ClothesType.TOP, null);
        ReflectionTestUtils.setField(clothes, "id", clothesId);
        UUID attrId = UUID.randomUUID();
        ClothesAttributeWithDefDto attrDto = new ClothesAttributeWithDefDto(
            attrId,
            "계절",
            List.of("봄", "여름", "가을", "겨울"),
            "겨울"
        );
        ClothesUpdateRequest request = new ClothesUpdateRequest("새로운셔츠", ClothesType.OUTER, null);
        ClothesDto expected = ClothesDto.builder()
            .id(clothesId)
            .ownerId(ownerId)
            .name("새로운셔츠")
            .type(ClothesType.OUTER)
            .imageUrl(null)
            .attributes(List.of(attrDto))
            .build();

        given(clothesRepository.findById(clothesId)).willReturn(Optional.of(clothes));
        given(clothesMapper.toDto(any(Clothes.class))).willReturn(expected);

        // when
        ClothesDto result = clothesService.update(clothesId, request, null);

        // then
        assertThat(result.name()).isEqualTo("새로운셔츠");
        assertThat(result.type()).isEqualTo(ClothesType.OUTER);
    }

    @Test
    void 의상_수정시_이미지교체_성공() {
        // given
        UUID clothesId = UUID.randomUUID();
        Clothes clothes = ClothesFixture.createClothesEntity(owner, "운동화", ClothesType.SHOES,
            "old/image.png");
        ReflectionTestUtils.setField(clothes, "id", clothesId);

        ClothesUpdateRequest request = new ClothesUpdateRequest(null, null, null);
        MultipartFile newImage = new MockMultipartFile(
            "image", "new.png", "image/png", "fake".getBytes()
        );
        ClothesDto expected = ClothesDto.builder()
            .id(clothesId)
            .ownerId(ownerId)
            .name("운동화")
            .type(ClothesType.SHOES)
            .imageUrl("new/image.png")
            .attributes(List.of())
            .build();

        given(clothesRepository.findById(clothesId)).willReturn(Optional.of(clothes));
        given(fileStorage.upload(eq("new.png"), any(InputStream.class), eq("image/png"), any()))
            .willReturn("new/image.png");
        given(clothesMapper.toDto(any(Clothes.class))).willReturn(expected);

        // when
        ClothesDto result = clothesService.update(clothesId, request, newImage);

        // then
        assertThat(result.imageUrl()).isEqualTo("new/image.png");
        verify(fileStorage).delete("old/image.png");
    }

    @Test
    void 의상_수정시_속성값을_추가할_수있다() {
        // given
        UUID clothesId = UUID.randomUUID();
        Clothes clothes = ClothesFixture.createClothesEntity(owner, "청바지", ClothesType.BOTTOM,
            null);
        ReflectionTestUtils.setField(clothes, "id", clothesId);

        UUID attrId = UUID.randomUUID();
        ClothesUpdateRequest request = new ClothesUpdateRequest(
            null, null, List.of(new ClothesAttributeDto(attrId, "겨울"))
        );
        ClothesAttribute attribute = ClothesFixture.createSeasonAttribute(attrId);
        ClothesAttributeWithDefDto attrDto = new ClothesAttributeWithDefDto(
            attrId,
            "계절",
            List.of("봄", "여름", "가을", "겨울"),
            "겨울"
        );
        ClothesDto expected = ClothesDto.builder()
            .id(clothesId)
            .ownerId(ownerId)
            .name("청바지")
            .type(ClothesType.BOTTOM)
            .imageUrl(null)
            .attributes(List.of(attrDto))
            .build();
        given(clothesRepository.findById(clothesId)).willReturn(Optional.of(clothes));
        given(clothesAttributeRepository.findById(attrId)).willReturn(Optional.of(attribute));
        given(clothesMapper.toDto(any(Clothes.class))).willReturn(expected);

        // when
        ClothesDto result = clothesService.update(clothesId, request, null);

        // then
        assertThat(result.attributes()).extracting("value").contains("겨울");
    }

    @Test
    void 의상_수정시_존재하지않으면_예외발생() {
        // given
        UUID clothesId = UUID.randomUUID();
        ClothesUpdateRequest request = new ClothesUpdateRequest("이름", ClothesType.TOP, null);

        given(clothesRepository.findById(clothesId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clothesService.update(clothesId, request, null))
            .isInstanceOf(
                com.sprint.ootd5team.base.exception.clothes.ClothesNotFoundException.class);
    }

    @Test
    void 의상_수정시_속성값_변경() {
        // given
        UUID clothesId = UUID.randomUUID();
        UUID attrId = UUID.randomUUID();

        // 기존 속성: "여름"
        ClothesAttribute attribute = ClothesFixture.createSeasonAttribute(attrId);
        Clothes clothes = ClothesFixture.createClothesEntity(owner, "셔츠", ClothesType.TOP, null);
        ClothesAttributeValue existingValue = new ClothesAttributeValue(clothes, attribute, "여름");
        clothes.addClothesAttributeValue(existingValue);
        ReflectionTestUtils.setField(clothes, "id", clothesId);

        // 요청: 같은 속성 정의지만 값은 "겨울"
        ClothesUpdateRequest request = new ClothesUpdateRequest(
            null,
            null,
            List.of(new ClothesAttributeDto(attrId, "겨울"))
        );

        ClothesAttributeWithDefDto updatedAttrDto = new ClothesAttributeWithDefDto(
            attrId,
            "계절",
            List.of("봄", "여름", "가을", "겨울"),
            "겨울"
        );

        ClothesDto expected = ClothesDto.builder()
            .id(clothesId)
            .ownerId(ownerId)
            .name("셔츠")
            .type(ClothesType.TOP)
            .imageUrl(null)
            .attributes(List.of(updatedAttrDto))
            .build();

        given(clothesRepository.findById(clothesId)).willReturn(Optional.of(clothes));
        given(clothesAttributeRepository.findById(attrId)).willReturn(Optional.of(attribute));
        given(clothesMapper.toDto(any(Clothes.class))).willReturn(expected);

        // when
        ClothesDto result = clothesService.update(clothesId, request, null);

        // then
        assertThat(result.attributes()).extracting("value").containsExactly("겨울");
    }

    @Test
    void 의상_삭제_성공() {
        // given
        UUID clothesId = UUID.randomUUID();
        Clothes clothes = ClothesFixture.createClothesEntity(owner, "셔츠", ClothesType.TOP, null);

        given(clothesRepository.findById(clothesId)).willReturn(Optional.of(clothes));

        // when
        clothesService.delete(ownerId, clothesId);

        // then
        verify(clothesRepository).deleteById(clothesId);
    }

    @Test
    void 의상_삭제시_이미지가_있으면_스토리지에서도_삭제된다() {
        // given
        UUID clothesId = UUID.randomUUID();
        Clothes clothes = ClothesFixture.createClothesEntity(owner, "셔츠", ClothesType.TOP,
            "image.png");

        given(clothesRepository.findById(clothesId)).willReturn(Optional.of(clothes));

        // when
        clothesService.delete(ownerId, clothesId);

        // then
        verify(fileStorage).delete("image.png");
        verify(clothesRepository).deleteById(clothesId);
    }

    @Test
    void 의상_삭제시_이미지삭제에_실패해도_DB삭제는_진행된다() {
        // given
        UUID clothesId = UUID.randomUUID();
        Clothes clothes = ClothesFixture.createClothesEntity(owner, "셔츠", ClothesType.TOP,
            "image.png");

        given(clothesRepository.findById(clothesId)).willReturn(Optional.of(clothes));
        willThrow(new RuntimeException("스토리지 오류")).given(fileStorage).delete("image.png");

        // when
        clothesService.delete(ownerId, clothesId);

        // then
        verify(fileStorage).delete("image.png");
        verify(clothesRepository).deleteById(clothesId);
    }

    @Test
    void 의상_삭제시_이미지가없으면_스토리지호출없음() {
        // given
        UUID clothesId = UUID.randomUUID();
        Clothes clothes = ClothesFixture.createClothesEntity(owner, "셔츠", ClothesType.TOP, null);

        given(clothesRepository.findById(clothesId)).willReturn(Optional.of(clothes));

        // when
        clothesService.delete(ownerId, clothesId);

        // then
        verify(fileStorage, never()).delete(any());
        verify(clothesRepository).deleteById(clothesId);
    }

    @Test
    void 의상_삭제시_존재하지않으면_예외() {
        // given
        UUID clothesId = UUID.randomUUID();
        given(clothesRepository.findById(clothesId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clothesService.delete(ownerId, clothesId))
            .isInstanceOf(ClothesNotFoundException.class);
        verify(clothesRepository, never()).deleteById(any());
    }

    @Test
    void 의상_삭제시_소유자아니면_예외() {
        // given
        UUID clothesId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID(); // 다른 사용자
        Clothes clothes = ClothesFixture.createClothesEntity(owner, "청바지", ClothesType.BOTTOM,
            null);

        given(clothesRepository.findById(clothesId)).willReturn(Optional.of(clothes));

        // when & then
        assertThatThrownBy(() -> clothesService.delete(anotherUserId, clothesId))
            .isInstanceOf(SecurityException.class);
        verify(clothesRepository, never()).deleteById(any());
    }
}
