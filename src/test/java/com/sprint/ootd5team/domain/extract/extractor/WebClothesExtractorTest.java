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
        ClothesAttribute colorAttr = ClothesFixture.createColorAttribute(UUID.randomUUID());
        given(clothesAttributeRepository.findAllWithDefs())
            .willReturn(List.of(colorAttr));

        extractor.initCache();
    }

    @Test
    void url을_통해_의상_정보를_정상적으로_추출한다() {
        // given
        String url = "https://dummy.com/product/1";
        BasicClothesInfo basic = new BasicClothesInfo("https://dummy.com/img.png", "본문 텍스트", "이름", url);
        ClothesExtraInfo extra = new ClothesExtraInfo("테스트 자켓", "아우터", Map.of("색상", "화이트"));
        given(metadataExtractionService.extract(url)).willReturn(basic);
        given(llmExtractionService.extractExtra(basic)).willReturn(extra);
        ClothesAttribute colorAttr = clothesAttributeRepository.findAllWithDefs().get(0);
        ClothesAttributeValue value = new ClothesAttributeValue(colorAttr, "화이트");
        ClothesAttributeWithDefDto dto = new ClothesAttributeWithDefDto(
            colorAttr.getId(),
            "색상",
            List.of("그레이", "블랙", "화이트"),
            "화이트"
        );
        given(clothesAttributeMapper.toDto(any(ClothesAttributeValue.class))).willReturn(dto);

        // when
        ClothesDto result = extractor.extractByUrl(url);

        // then
        assertThat(result.name()).isEqualTo("테스트 자켓");
        assertThat(result.imageUrl()).isEqualTo("https://dummy.com/img.png");
        assertThat(result.type().name()).isEqualTo("OUTER");
        assertThat(result.attributes()).hasSize(1);
        assertThat(result.attributes().get(0).definitionName()).isEqualTo("색상");
        assertThat(result.attributes().get(0).value()).isEqualTo("화이트");
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
        BasicClothesInfo basic = new BasicClothesInfo("https://dummy.com/img.png", "본문 텍스트", "이름", url);
        ClothesExtraInfo extra = new ClothesExtraInfo("테스트 바지", "하의", Map.of("없는속성", "값"));
        given(metadataExtractionService.extract(url)).willReturn(basic);
        given(llmExtractionService.extractExtra(basic)).willReturn(extra);

        // when
        ClothesDto result = extractor.extractByUrl(url);

        // then
        assertThat(result.attributes()).isEmpty();
    }

    @Test
    void 색상_정규화가_적용된다() {
        // given
        String url = "https://dummy.com/product/3";
        BasicClothesInfo basic = new BasicClothesInfo("https://dummy.com/img.png", "본문 텍스트", "이름", url);
        // LLM이 "라이트 그레이" 반환
        ClothesExtraInfo extra = new ClothesExtraInfo("테스트 후드티", "상의", Map.of("색상", "라이트 그레이"));
        given(metadataExtractionService.extract(url)).willReturn(basic);
        given(llmExtractionService.extractExtra(basic)).willReturn(extra);
        ClothesAttribute colorAttr = clothesAttributeRepository.findAllWithDefs().get(0);
        ClothesAttributeValue value = new ClothesAttributeValue(colorAttr, "그레이"); // normalize 결과값
        ClothesAttributeWithDefDto dto = new ClothesAttributeWithDefDto(
            colorAttr.getId(),
            "색상",
            List.of("그레이", "블랙", "기타"),
            "그레이"
        );
        given(clothesAttributeMapper.toDto(any(ClothesAttributeValue.class))).willReturn(dto);

        // when
        ClothesDto result = extractor.extractByUrl(url);

        // then
        assertThat(result.attributes()).hasSize(1);
        assertThat(result.attributes().get(0).definitionName()).isEqualTo("색상");
        assertThat(result.attributes().get(0).value()).isEqualTo("그레이");
    }

    @Test
    void 매칭되지않는값은_기타로_정규화된다() {
        // given
        String url = "https://dummy.com/product/4";
        BasicClothesInfo basic = new BasicClothesInfo("https://dummy.com/img.png", "본문 텍스트", "이름", url);

        // LLM이 매칭 안 되는 색상 반환
        ClothesExtraInfo extra = new ClothesExtraInfo("테스트 셔츠", "상의", Map.of("색상", "라이트 블루"));
        given(metadataExtractionService.extract(url)).willReturn(basic);
        given(llmExtractionService.extractExtra(basic)).willReturn(extra);

        // selectableValues: 블랙, 화이트, 기타
        ClothesAttribute colorAttr = clothesAttributeRepository.findAllWithDefs().get(0);
        given(clothesAttributeRepository.findAllWithDefs()).willReturn(List.of(colorAttr));
        extractor.initCache();

        ClothesAttributeValue value = new ClothesAttributeValue(colorAttr, "기타");
        ClothesAttributeWithDefDto dto = new ClothesAttributeWithDefDto(
            colorAttr.getId(),
            "색상",
            List.of("그레이", "기타", "화이트"),
            "기타"
        );
        given(clothesAttributeMapper.toDto(any(ClothesAttributeValue.class))).willReturn(dto);

        // when
        ClothesDto result = extractor.extractByUrl(url);

        // then
        assertThat(result.attributes()).hasSize(1);
        assertThat(result.attributes().get(0).definitionName()).isEqualTo("색상");
        assertThat(result.attributes().get(0).value()).isEqualTo("기타");
    }
}
