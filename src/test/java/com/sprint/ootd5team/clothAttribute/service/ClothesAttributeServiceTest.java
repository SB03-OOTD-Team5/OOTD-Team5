package com.sprint.ootd5team.clothAttribute.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.ootd5team.base.exception.clothesattribute.AttributeAlreadyExistException;
import com.sprint.ootd5team.base.exception.clothesattribute.AttributeNotFoundException;
import com.sprint.ootd5team.base.exception.clothesattribute.InvalidAttributeException;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefCreateRequest;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefDto;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefUpdateRequest;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeDef;
import com.sprint.ootd5team.domain.clothattribute.mapper.ClothesAttributeMapper;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothesAttributeRepository;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothesAttributeValueRepository;
import com.sprint.ootd5team.domain.clothattribute.service.BasicClothesAttributeService;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClothesAttributeService 테스트")
@TestClassOrder(ClassOrderer.DisplayName.class)
class ClothesAttributeServiceTest {

	@Mock
	private ClothesAttributeRepository clothesAttributeRepository;

	@Mock
	private ClothesAttributeMapper clothesAttributeMapper;

	@Mock
	private ClothesAttributeValueRepository clothesAttributeValueRepository;

	@Mock
	private EntityManager entityManager;

	@InjectMocks
	private BasicClothesAttributeService service;

	@BeforeEach
	void injectEntityManager() {
		ReflectionTestUtils.setField(service, "em", entityManager);
	}

	@Nested
	@DisplayName("1. 옷 속성 정의 생성")
	@TestClassOrder(ClassOrderer.DisplayName.class)
	class Create {

		@Test
		@DisplayName("1_1 생성: 새 속성과 정의 생성")
		void createAttribute() {
			// given
			ClothesAttributeDefCreateRequest request =
				new ClothesAttributeDefCreateRequest("소재", List.of("면", "울"));
			UUID generatedId = UUID.randomUUID();
			Instant createdAt = Instant.now();

			when(clothesAttributeRepository.existsByNameIgnoreCase("소재")).thenReturn(false);
			when(clothesAttributeRepository.save(any(ClothesAttribute.class)))
				.thenAnswer(invocation -> {
					ClothesAttribute attr = invocation.getArgument(0);
					ReflectionTestUtils.setField(attr, "id", generatedId);
					ReflectionTestUtils.setField(attr, "createdAt", createdAt);
					return attr;
				});

			ClothesAttributeDefDto expected =
				new ClothesAttributeDefDto(generatedId, "소재", List.of("면", "울"), createdAt);
			when(clothesAttributeMapper.toDto(any(ClothesAttribute.class))).thenReturn(expected);

			// when
			ClothesAttributeDefDto result = service.create(request);

			// then
			assertThat(result).isEqualTo(expected);
			ArgumentCaptor<ClothesAttribute> captor = ArgumentCaptor.forClass(ClothesAttribute.class);
			verify(clothesAttributeRepository).save(captor.capture());
			assertThat(captor.getValue().getName()).isEqualTo("소재");
			assertThat(captor.getValue().getDefs()).hasSize(2);
		}

		@Nested
		@DisplayName("예외 처리")
		@TestMethodOrder(MethodOrderer.DisplayName.class)
		class Exceptions {

			@Test
			@DisplayName("1_2_1 생성_예외: 속성명이 공백이면 InvalidAttributeException")
			void blankName() {
				ClothesAttributeDefCreateRequest request =
					new ClothesAttributeDefCreateRequest("  ", List.of("면"));

				assertThatThrownBy(() -> service.create(request))
					.isInstanceOf(InvalidAttributeException.class);
				verify(clothesAttributeRepository, never()).save(any());
			}

			@Test
			@DisplayName("1_2_2 생성_예외: 중복된 속성명이면 AttributeAlreadyExistException")
			void duplicatedName() {
				ClothesAttributeDefCreateRequest request =
					new ClothesAttributeDefCreateRequest("소재", List.of("면"));

				when(clothesAttributeRepository.existsByNameIgnoreCase("소재")).thenReturn(true);

				assertThatThrownBy(() -> service.create(request))
					.isInstanceOf(AttributeAlreadyExistException.class);
				verify(clothesAttributeRepository, never()).save(any());
			}
		}
	}

	@Nested
	@DisplayName("2. 옷 속성 정의 전체정렬,조회")
	@TestClassOrder(ClassOrderer.DisplayName.class)
	class FindAll {

		@Test
		@DisplayName("2_1 조회: 이름 기준 오름차순 정렬 및 키워드 필터링")
		void filterAndSortByName() {
			// given
			ClothesAttribute style = new ClothesAttribute("스타일");
			ClothesAttribute material = new ClothesAttribute("소재");

			when(clothesAttributeRepository.findAll()).thenReturn(List.of(style, material));

			ClothesAttributeDefDto styleDto =
				new ClothesAttributeDefDto(UUID.randomUUID(), "스타일", List.of("캐주얼"), Instant.parse("2024-01-01T00:00:00Z"));
			ClothesAttributeDefDto materialDto =
				new ClothesAttributeDefDto(UUID.randomUUID(), "소재", List.of("면"), Instant.parse("2024-01-02T00:00:00Z"));

			when(clothesAttributeMapper.toDto(style)).thenReturn(styleDto);
			when(clothesAttributeMapper.toDto(material)).thenReturn(materialDto);

			// when
			List<ClothesAttributeDefDto> result = service.findAll("name", "ASCENDING", "소");

			// then
			assertThat(result).hasSize(1);
			assertThat(result.get(0).name()).isEqualTo("소재");
		}

		@Test
		@DisplayName("2_2 조회: 생성일 기준 내림차순 정렬")
		void sortByCreatedAtDescending() {
			ClothesAttribute first = new ClothesAttribute("첫번째");
			ClothesAttribute second = new ClothesAttribute("두번째");

			when(clothesAttributeRepository.findAll()).thenReturn(List.of(first, second));

			ClothesAttributeDefDto firstDto =
				new ClothesAttributeDefDto(UUID.randomUUID(), "첫번째", List.of(), Instant.parse("2024-01-01T00:00:00Z"));
			ClothesAttributeDefDto secondDto =
				new ClothesAttributeDefDto(UUID.randomUUID(), "두번째", List.of(), Instant.parse("2024-01-03T00:00:00Z"));

			when(clothesAttributeMapper.toDto(first)).thenReturn(firstDto);
			when(clothesAttributeMapper.toDto(second)).thenReturn(secondDto);

			List<ClothesAttributeDefDto> result = service.findAll("createdAt", "DESCENDING", null);

			assertThat(result).extracting(ClothesAttributeDefDto::name)
				.containsExactly("두번째", "첫번째");
		}


		@Nested
		@DisplayName("예외 처리")
		@TestMethodOrder(MethodOrderer.DisplayName.class)
		class Exceptions {

			@Test
			@DisplayName("2_3_1 조회 예외: 정렬 방향이 잘못되어도 기본 오름차순으로 동작")
			void invalidSortDirectionGracefullyHandled() {
				ClothesAttribute a = new ClothesAttribute("A");
				ClothesAttribute b = new ClothesAttribute("B");

				when(clothesAttributeRepository.findAll()).thenReturn(List.of(b, a));

				ClothesAttributeDefDto aDto =
					new ClothesAttributeDefDto(UUID.randomUUID(), "A", List.of(), Instant.now());
				ClothesAttributeDefDto bDto =
					new ClothesAttributeDefDto(UUID.randomUUID(), "B", List.of(), Instant.now());

				when(clothesAttributeMapper.toDto(a)).thenReturn(aDto);
				when(clothesAttributeMapper.toDto(b)).thenReturn(bDto);

				List<ClothesAttributeDefDto> result = service.findAll("name", "WRONG", null);

				assertThat(result).extracting(ClothesAttributeDefDto::name)
					.containsExactly("A", "B");
			}
		}
	}

	@Nested
	@DisplayName("3. 옷 속성 정의 수정")
	@TestClassOrder(ClassOrderer.DisplayName.class)
	class Update {

		@Test
		@DisplayName("3_1 수정: 속성명과 정의 수정")
		void updateAttribute() {
			UUID attributeId = UUID.randomUUID();
			ClothesAttribute attribute = new ClothesAttribute("소재");
			attribute.addDef(new ClothesAttributeDef(attribute, "면"));
			ReflectionTestUtils.setField(attribute, "id", attributeId);

			when(clothesAttributeRepository.findById(attributeId)).thenReturn(Optional.of(attribute));
			when(clothesAttributeRepository.existsByNameIgnoreCase("재질")).thenReturn(false);
			when(clothesAttributeRepository.save(attribute)).thenReturn(attribute);
			doNothing().when(entityManager).flush();

			ClothesAttributeDefDto expected =
				new ClothesAttributeDefDto(attributeId, "재질", List.of("울", "실크"), Instant.now());
			when(clothesAttributeMapper.toDto(attribute)).thenReturn(expected);

			ClothesAttributeDefUpdateRequest request =
				new ClothesAttributeDefUpdateRequest(" 재질 ", List.of(" 울 ", " 실크 "));

			ClothesAttributeDefDto result = service.update(attributeId, request);

			assertThat(result).isEqualTo(expected);
			assertThat(attribute.getName()).isEqualTo("재질");
			assertThat(attribute.getDefs()).extracting(ClothesAttributeDef::getAttDef)
				.containsExactlyInAnyOrder("울", "실크");
			verify(entityManager).flush();
		}

		@Nested
		@DisplayName("예외 처리")
		@TestMethodOrder(MethodOrderer.DisplayName.class)
		class Exceptions {

			@Test
			@DisplayName("3_2_1 수정 예외: 대상 속성이 없으면 AttributeNotFoundException 반환")
			void attributeNotFound() {
				UUID attributeId = UUID.randomUUID();
				when(clothesAttributeRepository.findById(attributeId)).thenReturn(Optional.empty());

				assertThatThrownBy(() -> service.update(attributeId,
					new ClothesAttributeDefUpdateRequest("소재", List.of("면"))))
					.isInstanceOf(AttributeNotFoundException.class);
				verify(entityManager, never()).flush();
			}

			@Test
			@DisplayName("3_2_2 수정 예외: 새 이름이 공백이면 InvalidAttributeException 반환")
			void blankNewName() {
				UUID attributeId = UUID.randomUUID();
				ClothesAttribute attribute = new ClothesAttribute("소재");
				when(clothesAttributeRepository.findById(attributeId)).thenReturn(Optional.of(attribute));

				assertThatThrownBy(() -> service.update(attributeId,
					new ClothesAttributeDefUpdateRequest("  ", List.of("면"))))
					.isInstanceOf(InvalidAttributeException.class);
				verify(clothesAttributeRepository, never()).save(any());
			}

			@Test
			@DisplayName("3_2_3 수정 예외: 다른 속성명이 이미 존재하면 AttributeAlreadyExistException 반환")
			void duplicatedNewName() {
				UUID attributeId = UUID.randomUUID();
				ClothesAttribute attribute = new ClothesAttribute("소재");
				when(clothesAttributeRepository.findById(attributeId)).thenReturn(Optional.of(attribute));
				when(clothesAttributeRepository.existsByNameIgnoreCase("재질")).thenReturn(true);

				assertThatThrownBy(() -> service.update(attributeId,
					new ClothesAttributeDefUpdateRequest("재질", List.of("면"))))
					.isInstanceOf(AttributeAlreadyExistException.class);
				verify(clothesAttributeRepository, never()).save(any());
			}
		}
	}

	@Nested
	@DisplayName("4. 옷 속성 정의 삭제")
	@TestClassOrder(ClassOrderer.DisplayName.class)
	class Delete {

		@Test
		@DisplayName("4_1 삭제: 속성과 속성 정의 삭제")
		void deleteAttribute() {
			UUID attributeId = UUID.randomUUID();
			ClothesAttribute attribute = new ClothesAttribute("소재");
			attribute.addDef(new ClothesAttributeDef(attribute, "면"));

			when(clothesAttributeRepository.findById(attributeId)).thenReturn(Optional.of(attribute));
			when(clothesAttributeValueRepository.existsByAttribute_Id(attributeId)).thenReturn(false);
			doNothing().when(entityManager).flush();

			service.delete(attributeId);

			assertThat(attribute.getDefs()).isEmpty();
			verify(entityManager).flush();
			verify(clothesAttributeRepository).delete(attribute);
		}


		@Nested
		@DisplayName("예외 처리")
		@TestMethodOrder(MethodOrderer.DisplayName.class)
		class Exceptions {

			@Test
			@DisplayName("4_2_1 삭제 예외: 대상 속성이 없으면 AttributeNotFoundException")
			void attributeNotFound() {
				UUID attributeId = UUID.randomUUID();
				when(clothesAttributeRepository.findById(attributeId)).thenReturn(Optional.empty());

				assertThatThrownBy(() -> service.delete(attributeId))
					.isInstanceOf(AttributeNotFoundException.class);
				verify(clothesAttributeRepository, never()).delete(any());
			}

			@Test
			@DisplayName("4_2_2 삭제 예외: 속성에 연결된 정의가 존재하면 InvalidAttributeException")
			void attributeInUse() {
				UUID attributeId = UUID.randomUUID();
				ClothesAttribute attribute = new ClothesAttribute("소재");
				when(clothesAttributeRepository.findById(attributeId)).thenReturn(Optional.of(attribute));
				when(clothesAttributeValueRepository.existsByAttribute_Id(attributeId)).thenReturn(true);

				assertThatThrownBy(() -> service.delete(attributeId))
					.isInstanceOf(InvalidAttributeException.class);
				verify(clothesAttributeRepository, never()).delete(any());
			}
		}
	}
}
