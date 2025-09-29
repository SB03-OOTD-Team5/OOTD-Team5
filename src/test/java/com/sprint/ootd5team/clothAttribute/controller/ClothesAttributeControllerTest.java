package com.sprint.ootd5team.clothAttribute.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.exception.GlobalExceptionHandler;
import com.sprint.ootd5team.base.exception.clothesattribute.AttributeAlreadyExistException;
import com.sprint.ootd5team.base.exception.clothesattribute.AttributeNotFoundException;
import com.sprint.ootd5team.base.exception.clothesattribute.InvalidAttributeException;
import com.sprint.ootd5team.domain.clothattribute.controller.ClothesAttributeController;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefCreateRequest;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefDto;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefUpdateRequest;
import com.sprint.ootd5team.domain.clothattribute.service.ClothesAttributeService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClothesAttributeController 단위 테스트")
@TestClassOrder(ClassOrderer.DisplayName.class)
class ClothesAttributeControllerTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Mock
	private ClothesAttributeService clothesAttributeService;

	@InjectMocks
	private ClothesAttributeController controller;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
			.standaloneSetup(controller)
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();
	}

	@Nested
	@DisplayName("1. 속성 정의 등록")
	class Register {

		@Test
		@DisplayName("성공: 응답코드=201, 반환=ClothesAttributeDefDto")
		void registerAttributeDef() throws Exception {
			ClothesAttributeDefCreateRequest request =
				new ClothesAttributeDefCreateRequest("소재", List.of("면", "울"));
			ClothesAttributeDefDto responseDto =
				new ClothesAttributeDefDto(UUID.randomUUID(), "소재", List.of("면", "울"), Instant.now());

			when(clothesAttributeService.create(any(ClothesAttributeDefCreateRequest.class)))
				.thenReturn(responseDto);

			mockMvc.perform(post("/api/clothes/attribute-defs")
					.contentType(MediaType.APPLICATION_JSON)
					.content(OBJECT_MAPPER.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("소재"))
				.andExpect(jsonPath("$.selectableValues[0]").value("면"));

			verify(clothesAttributeService).create(any(ClothesAttributeDefCreateRequest.class));
		}

		@Test
		@DisplayName("실패: 응답코드=409, 조건=속성명 중복, 반환=ATTRIBUTE_ALREADY_EXIST")
		void duplicateName() throws Exception {
			ClothesAttributeDefCreateRequest request =
				new ClothesAttributeDefCreateRequest("소재", List.of("면"));

			when(clothesAttributeService.create(any(ClothesAttributeDefCreateRequest.class)))
				.thenThrow(AttributeAlreadyExistException.withName("소재"));

			mockMvc.perform(post("/api/clothes/attribute-defs")
					.contentType(MediaType.APPLICATION_JSON)
					.content(OBJECT_MAPPER.writeValueAsString(request)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.exceptionName").value("ATTRIBUTE_ALREADY_EXIST"))
				.andExpect(jsonPath("$.details.name").value("소재"));

			verify(clothesAttributeService).create(any(ClothesAttributeDefCreateRequest.class));
		}

	}

	@Nested
	@DisplayName("2. 속성 정의 목록 조회")
	class ListAttributes {

		@Test
		@DisplayName("성공: 응답코드= 200, 반환= List<ClothesAttributeDefDto>")
		void listAttributes() throws Exception {
			ClothesAttributeDefDto first =
				new ClothesAttributeDefDto(UUID.randomUUID(), "소재", List.of("면", "울"), Instant.now());
			ClothesAttributeDefDto second =
				new ClothesAttributeDefDto(UUID.randomUUID(), "계절", List.of("봄", "여름"), Instant.now());

			when(clothesAttributeService.findAll("createdAt", "ASCENDING", null))
				.thenReturn(List.of(first, second));

			mockMvc.perform(get("/api/clothes/attribute-defs")
					.param("sortBy", "createdAt")
					.param("sortDirection", "ASCENDING"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("소재"))
				.andExpect(jsonPath("$[1].name").value("계절"));

			verify(clothesAttributeService)
				.findAll("createdAt", "ASCENDING", null);
		}

		@Test
		@DisplayName("실패: 응답코드= 400, 조건= 정렬옵션불일치, 반환= INVALID_ATTRIBUTE")
		void invalidSortOption() throws Exception {
			when(clothesAttributeService.findAll("wrong", "DESC", "keyword"))
				.thenThrow(new InvalidAttributeException());

			mockMvc.perform(get("/api/clothes/attribute-defs")
					.param("sortBy", "wrong")
					.param("sortDirection", "DESC")
					.param("keywordLike", "keyword"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.exceptionName").value("INVALID_ATTRIBUTE"))
				.andExpect(jsonPath("$.message").isString());

			verify(clothesAttributeService)
				.findAll("wrong", "DESC", "keyword");
		}

	}

	@Nested
	@DisplayName("3. 속성 정의 수정")
	class UpdateAttribute {

		@Test
		@DisplayName("성공: 응답코드= 200, 반환= ClothesAttributeDefDto")
		void updateAttribute() throws Exception {
			UUID id = UUID.randomUUID();
			ClothesAttributeDefUpdateRequest request =
				new ClothesAttributeDefUpdateRequest("재질", List.of("울", "실크"));
			ClothesAttributeDefDto updated =
				new ClothesAttributeDefDto(id, "재질", List.of("울", "실크"), Instant.now());

			when(clothesAttributeService.update(eq(id), any(ClothesAttributeDefUpdateRequest.class)))
				.thenReturn(updated);

			mockMvc.perform(patch("/api/clothes/attribute-defs/{id}", id)
					.contentType(MediaType.APPLICATION_JSON)
					.content(OBJECT_MAPPER.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("재질"))
				.andExpect(jsonPath("$.selectableValues[1]").value("실크"));

			verify(clothesAttributeService)
				.update(eq(id), any(ClothesAttributeDefUpdateRequest.class));
		}

		@Test
		@DisplayName("실패: 응답코드= 404, 조건= 조회Id상이, 반환= ATTRIBUTE_NOT_FOUND")
		void attributeNotFound() throws Exception {
			UUID id = UUID.randomUUID();
			ClothesAttributeDefUpdateRequest request =
				new ClothesAttributeDefUpdateRequest("재질", List.of("울"));

			when(clothesAttributeService.update(eq(id), any(ClothesAttributeDefUpdateRequest.class)))
				.thenThrow(AttributeNotFoundException.withId(id));

			mockMvc.perform(patch("/api/clothes/attribute-defs/{id}", id)
					.contentType(MediaType.APPLICATION_JSON)
					.content(OBJECT_MAPPER.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.exceptionName").value("ATTRIBUTE_NOT_FOUND"))
				.andExpect(jsonPath("$.details.attributeId").value(id.toString()));

			verify(clothesAttributeService)
				.update(eq(id), any(ClothesAttributeDefUpdateRequest.class));
		}

	}

	@Nested
	@DisplayName("4. 속성 정의 삭제")
	class DeleteAttribute {

		@Test
		@DisplayName("성공: 응답코드= 204")
		void deleteAttribute() throws Exception {
			UUID id = UUID.randomUUID();
			doNothing().when(clothesAttributeService).delete(id);

			mockMvc.perform(delete("/api/clothes/attribute-defs/{id}", id))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));

			verify(clothesAttributeService).delete(id);
		}

		@Test
		@DisplayName("실패: 응답코드= 404, 조건= 조회Id상이, 반환= ATTRIBUTE_NOT_FOUND")
		void attributeNotFound() throws Exception {
			UUID id = UUID.randomUUID();

		doThrow(AttributeNotFoundException.withId(id))
			.when(clothesAttributeService).delete(id);

			mockMvc.perform(delete("/api/clothes/attribute-defs/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.exceptionName").value("ATTRIBUTE_NOT_FOUND"))
				.andExpect(jsonPath("$.details.attributeId").value(id.toString()));

			verify(clothesAttributeService).delete(id);
		}

	}
}
