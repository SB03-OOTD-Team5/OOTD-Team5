package com.sprint.ootd5team.domain.extract.extractor;

import com.sprint.ootd5team.base.exception.clothes.ClothesExtractionFailedException;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeDef;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeValue;
import com.sprint.ootd5team.domain.clothattribute.mapper.ClothesAttributeMapper;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothesAttributeRepository;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;
import com.sprint.ootd5team.domain.clothes.fixture.ClothesFixture;
import com.sprint.ootd5team.domain.extract.dto.BasicClothesInfo;
import com.sprint.ootd5team.domain.extract.dto.ClothesExtraInfo;
import com.sprint.ootd5team.domain.extract.service.LlmExtractionService;
import com.sprint.ootd5team.domain.extract.service.MetadataExtractionService;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebClothesExtractor 단위 테스트")
class WebClothesExtractorTest {

    @Mock
    private MetadataExtractionService metadataExtractionService;

    @Mock
    private LlmExtractionService llmExtractionService;

    @Mock
    private ClothesAttributeRepository clothesAttributeRepository;

    @Mock
    private ClothesAttributeMapper clothesAttributeMapper;

    @InjectMocks
    private WebClothesExtractor extractor;

    @BeforeEach
    void setUp() {
        ClothesAttribute seasonAttr = ClothesFixture.createSeasonAttribute(UUID.randomUUID());
        given(clothesAttributeRepository.findAllWithDefs())
            .willReturn(List.of(seasonAttr));

        extractor.initCache();
    }

    @Test
    void url을_통해_의상_정보를_정상적으로_추출한다() {
        // given
        String url = "https://dummy.com/product/1";
        BasicClothesInfo basic = new BasicClothesInfo("https://dummy.com/img.png", "본문 텍스트");
        ClothesExtraInfo extra = new ClothesExtraInfo("테스트 자켓", "아우터", Map.of("계절", "봄"));
        given(metadataExtractionService.extract(url)).willReturn(basic);
        given(llmExtractionService.extractExtra("본문 텍스트")).willReturn(extra);
        ClothesAttribute seasonAttr = clothesAttributeRepository.findAllWithDefs().get(0);
        ClothesAttributeValue value = new ClothesAttributeValue(seasonAttr, "봄");
        ClothesAttributeWithDefDto dto = new ClothesAttributeWithDefDto(
            seasonAttr.getId(),
            "계절",
            List.of("봄", "여름", "가을", "겨울"),
            "봄"
        );
        given(clothesAttributeMapper.toDto(any(ClothesAttributeValue.class))).willReturn(dto);

        // when
        ClothesDto result = extractor.extractByUrl(url);

        // then
        assertThat(result.name()).isEqualTo("테스트 자켓");
        assertThat(result.imageUrl()).isEqualTo("https://dummy.com/img.png");
        assertThat(result.type().name()).isEqualTo("OUTER");
        assertThat(result.attributes()).hasSize(1);
        assertThat(result.attributes().get(0).definitionName()).isEqualTo("계절");
        assertThat(result.attributes().get(0).value()).isEqualTo("봄");
    }

    @Test
    void Metadata_추출시_예외가발생하면_ClothesExtractionFailedException을_던진다() {
        // given
        String url = "https://dummy.com/product/err";
        given(metadataExtractionService.extract(url)).willThrow(new RuntimeException("메타데이터 실패"));

        // when
        Throwable thrown = catchThrowable(() -> extractor.extractByUrl(url));

        // then
        assertThat(thrown)
                .isInstanceOf(ClothesExtractionFailedException.class)
                .hasMessageContaining("의상 정보 추출에 실패했습니다.");
    }

    @Test
    void llm결과에_매핑할_수_없는_속성은_무시된다() {
        // given
        String url = "https://dummy.com/product/2";
        BasicClothesInfo basic = new BasicClothesInfo("https://dummy.com/img.png", "본문 텍스트");
        ClothesExtraInfo extra = new ClothesExtraInfo("테스트 바지", "하의", Map.of("없는속성", "값"));
        given(metadataExtractionService.extract(url)).willReturn(basic);
        given(llmExtractionService.extractExtra("본문 텍스트")).willReturn(extra);

        // when
        ClothesDto result = extractor.extractByUrl(url);

        // then
        assertThat(result.attributes()).isEmpty();
    }
}
