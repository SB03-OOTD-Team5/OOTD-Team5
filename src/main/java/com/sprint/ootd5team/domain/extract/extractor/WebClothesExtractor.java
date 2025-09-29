package com.sprint.ootd5team.domain.extract.extractor;

import com.sprint.ootd5team.base.exception.clothes.ClothesExtractionFailedException;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeValue;
import com.sprint.ootd5team.domain.clothattribute.mapper.ClothesAttributeMapper;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothesAttributeRepository;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;
import com.sprint.ootd5team.domain.extract.dto.BasicClothesInfo;
import com.sprint.ootd5team.domain.extract.dto.ClothesExtraInfo;
import com.sprint.ootd5team.domain.extract.service.LlmExtractionService;
import com.sprint.ootd5team.domain.extract.service.MetadataExtractionService;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 웹 페이지(상품 상세 URL)를 기반으로 의상 정보를 추출하는 서비스 구현체.
 * <p>
 * 처리 과정:
 * <ul>
 *   <li>1) {@link MetadataExtractionService} 를 사용하여 상품 이미지 url을 가져옴</li>
 *   <li>2) {@link LlmExtractionService} 를 통해 LLM 기반으로 추가 속성 정보를 보강(이름, 타입, 속성값)</li>
 *   <li>3) 추출된 속성을 {@link ClothesAttributeRepository} 에서 조회한 정의(defs)와 매핑</li>
 * </ul>
 *
 * <p>속성(Attribute)은 서버 기동 시점에 캐시(Map)로 초기화하여, 요청 시 DB 조회를 반복하지 않음.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebClothesExtractor implements ClothesExtractor {

    private final MetadataExtractionService metadataExtractionService;
    private final LlmExtractionService llmExtractionService;
    private final ClothesAttributeRepository clothesAttributeRepository;
    private final ClothesAttributeMapper clothesAttributeMapper;

    // 캐싱용 맵 (attributeName -> ClothesAttribute)
    private Map<String, ClothesAttribute> attributeCache;

    /**
     * 서버 시작 시 속성 정의(attribute + defs)를 모두 로드하여 캐시에 저장.
     * N+1 쿼리 방지 및 빠른 매핑을 위함.
     */
    @PostConstruct
    public void initCache() {
        log.info("[WebClothesExtractor] 의상 속성 캐시 초기화 시작");
        attributeCache = clothesAttributeRepository.findAllWithDefs().stream()
            .collect(Collectors.toMap(ClothesAttribute::getName, a -> a));
        log.info("[WebClothesExtractor] 의상 속성 캐시 초기화 완료: {}개", attributeCache.size());
    }

    @Override
    public ClothesDto extractByUrl(String url) {
        try {
            log.info("[WebClothesExtractor] URL 추출 시작: {}", url);

            // 1. 메타데이터 추출
            BasicClothesInfo basic = metadataExtractionService.extract(url);

            // 2. LLM 보강
            ClothesExtraInfo extra = llmExtractionService.extractExtra(basic.bodyText());

            // 3. 속성 매핑
            List<ClothesAttributeWithDefDto> attributes = buildAttributes(extra);
            log.info("[ExtractResult] name={}, type={}, attributes={}",
                extra.name(), extra.toClothesType(), attributes);

            log.info("[WebClothesExtractor] 추출 결과 - name={}, type={}, attributes={}",
                extra.name(), extra.toClothesType(), attributes);

            return ClothesDto.builder()
                .name(extra.name())
                .imageUrl(basic.imageUrl())
                .type(extra.toClothesType())
                .attributes(attributes)
                .build();

        } catch (Exception e) {
            log.error("웹스크래핑 실패: {}", url, e);
            throw ClothesExtractionFailedException.withUrl(url);
        }
    }

    /**
     * LLM에서 추출된 속성 정보를 캐시된 정의값과 매핑하여 DTO로 변환
     */
    private List<ClothesAttributeWithDefDto> buildAttributes(ClothesExtraInfo extra) {
        List<ClothesAttributeWithDefDto> list = new ArrayList<>();

        if (extra.attributes() != null) {
            extra.attributes().forEach((attrName, value) -> {
                if (value != null && !value.isBlank()) {
                    ClothesAttribute attr = attributeCache.get(attrName);
                    if (attr != null) {
                        list.add(clothesAttributeMapper.toDto(new ClothesAttributeValue(attr, value)));
                    }
                }
            });
        }
        return list;
    }
}