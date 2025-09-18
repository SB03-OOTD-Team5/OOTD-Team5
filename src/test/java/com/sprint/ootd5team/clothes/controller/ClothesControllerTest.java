package com.sprint.ootd5team.clothes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.clothes.controller.ClothesController;
import com.sprint.ootd5team.domain.clothes.dto.request.ClothesCreateRequest;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothes.service.ClothesService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(controllers = ClothesController.class)
@DisplayName("ClothesController 슬라이스 테스트")
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ClothesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClothesService clothesService;

    private UUID ownerId;
    private List<ClothesDto> mockClothes;
    private ClothesDtoCursorResponse mockResponse;

    @BeforeEach
    void setUp() {
        ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        ClothesDto clothes1 = new ClothesDto(
            UUID.randomUUID(),
            ownerId,
            "흰 티셔츠",
            null,
            ClothesType.TOP,
            List.of(),
            Instant.now(),
            Instant.now()
        );

        ClothesDto clothes2 = new ClothesDto(
            UUID.randomUUID(),
            ownerId,
            "청바지",
            null,
            ClothesType.BOTTOM,
            List.of(),
            Instant.now(),
            Instant.now()
        );

        mockClothes = List.of(clothes1, clothes2);

        mockResponse = new ClothesDtoCursorResponse(
            mockClothes,
            null,
            null,
            false, // hasNext
            (long) mockClothes.size(),
            "createdAt",
            "DESC"
        );
    }

    @Test
    void 옷_목록을_기본값으로_조회한다() throws Exception {
        // given
        given(clothesService.getClothes(eq(ownerId), any(), any(), any(), anyInt()))
            .willReturn(mockResponse);

        // when
        ResultActions result = mockMvc.perform(get("/api/clothes")
            .param("ownerId", ownerId.toString())
            .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].name").value("흰 티셔츠"))
            .andExpect(jsonPath("$.data[1].name").value("청바지"))
            .andExpect(jsonPath("$.hasNext").value(false))
            .andExpect(jsonPath("$.totalCount").value(2));
    }

    @Test
    void 의상_생성_성공_이미지와_속성값_존재() throws Exception {
        // given
        UUID ownerId = UUID.randomUUID();
        UUID attributeId = UUID.randomUUID();

        // request JSON
        String requestJson = """
            {
              "ownerId": "%s",
              "name": "멋쟁이패딩",
              "type": "OUTER",
              "attributes": [
                {
                  "definitionId": "%s",
                  "definitionName": "계절",
                  "selectableValues": ["봄","여름","가을","겨울"],
                  "value": "겨울"
                }
              ]
            }
            """.formatted(ownerId, attributeId);

        // Mock MultipartFile → 실제로는 MockMvc에서 body만 전달
        MockMultipartFile requestPart = new MockMultipartFile(
            "request", "", "application/json", requestJson.getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile imagePart = new MockMultipartFile(
            "image", "coat.png", "image/png", "fake-image".getBytes()
        );
        ClothesDto expectedDto = new ClothesDto(
            UUID.randomUUID(),
            ownerId,
            "멋쟁이패딩",
            "clothes/uuid_coat.png",
            ClothesType.OUTER,
            List.of(
                new ClothesAttributeWithDefDto(attributeId, "계절", List.of("봄", "여름", "가을", "겨울"),
                    "겨울")
            ),
            Instant.now(),
            null
        );
        given(clothesService.create(any(ClothesCreateRequest.class), any(MultipartFile.class)))
            .willReturn(expectedDto);

        // when
        ResultActions result = mockMvc.perform(multipart("/api/clothes")
            .file(requestPart)
            .file(imagePart)
            .contentType(MediaType.MULTIPART_FORM_DATA));

        // then
        result.andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("멋쟁이패딩"))
            .andExpect(jsonPath("$.type").value("OUTER"))
            .andExpect(jsonPath("$.imageUrl").value("clothes/uuid_coat.png"));
        verify(clothesService).create(any(ClothesCreateRequest.class), any(MultipartFile.class));
    }

    @Test
    @DisplayName("의상 수정 성공 - 이미지와 속성값 변경")
    void 의상_수정_성공() throws Exception {
        // given
        UUID clothesId = UUID.randomUUID();
        UUID attributeId = UUID.randomUUID();

        String requestJson = """
        {
          "name": "수정된 패딩",
          "type": "OUTER",
          "attributes": [
            {
              "definitionId": "%s",
              "definitionName": "계절",
              "selectableValues": ["봄","여름","가을","겨울"],
              "value": "봄"
            }
          ]
        }
        """.formatted(attributeId);

        MockMultipartFile requestPart = new MockMultipartFile(
            "request", "", "application/json", requestJson.getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile imagePart = new MockMultipartFile(
            "image", "new_coat.png", "image/png", "fake-image".getBytes()
        );

        ClothesDto updatedDto = new ClothesDto(
            clothesId,
            ownerId,
            "수정된 패딩",
            "clothes/uuid_new_coat.png",
            ClothesType.OUTER,
            List.of(new ClothesAttributeWithDefDto(
                attributeId, "계절", List.of("봄","여름","가을","겨울"), "봄"
            )),
            Instant.now(),
            Instant.now()
        );

        given(clothesService.update(eq(clothesId), any(), any(MultipartFile.class)))
            .willReturn(updatedDto);

        // when
        ResultActions result = mockMvc.perform(
            multipart(HttpMethod.PATCH, "/api/clothes/{clothesId}", clothesId)
                .file(requestPart)
                .file(imagePart)
                .contentType(MediaType.MULTIPART_FORM_DATA));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(clothesId.toString()))
            .andExpect(jsonPath("$.name").value("수정된 패딩"))
            .andExpect(jsonPath("$.type").value("OUTER"))
            .andExpect(jsonPath("$.imageUrl").value("clothes/uuid_new_coat.png"))
            .andExpect(jsonPath("$.attributes[0].value").value("봄"));

        verify(clothesService).update(eq(clothesId), any(), any(MultipartFile.class));
    }
}
