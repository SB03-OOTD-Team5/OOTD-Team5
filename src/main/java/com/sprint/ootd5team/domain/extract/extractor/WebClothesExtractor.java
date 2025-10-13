package com.sprint.ootd5team.domain.extract.extractor;

import com.sprint.ootd5team.base.exception.clothes.ClothesExtractionFailedException;


import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothesattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothesattribute.mapper.ClothesAttributeMapper;
import com.sprint.ootd5team.domain.clothesattribute.repository.ClothesAttributeRepository;
import com.sprint.ootd5team.domain.extract.dto.BasicClothesInfo;
import com.sprint.ootd5team.domain.extract.dto.ClothesExtraInfo;
import com.sprint.ootd5team.domain.extract.service.LlmExtractionService;
import com.sprint.ootd5team.domain.extract.service.MetadataExtractionService;
import com.sprint.ootd5team.domain.extract.util.ClothesAttributeUtil;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
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
@Getter
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

//  후에 ClothesAttributeService에서 캐시 추상화 사용 가능
//    @Cacheable("clothesAttributes")
//    public Map<String, ClothesAttribute> getAttributeCache() {
//        return clothesAttributeRepository.findAllWithDefs().stream()
//            .collect(Collectors.toMap(ClothesAttribute::getName, a -> a));
//    }

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
            BasicClothesInfo basicInfo = metadataExtractionService.extract(url);

            // 2. LLM 보강
            ClothesExtraInfo extraInfo = llmExtractionService.extractExtra(basicInfo, attributeCache);

            // 3. 속성 매핑
            List<ClothesAttributeWithDefDto> attributes =
                ClothesAttributeUtil.buildAttributes(extraInfo, attributeCache,
                    clothesAttributeMapper);
            log.info("[WebClothesExtractor] 추출 결과 - name={}, type={}, attributes={}",
                extraInfo.name(), extraInfo.typeRaw(), attributes);

            // 4. 타입 변환
            ClothesType type = ClothesType.fromString(extraInfo.typeRaw());

            return ClothesDto.builder()
                .name(extraInfo.name())
                .imageUrl(basicInfo.imageUrl())
                .type(type)
                .attributes(attributes)
                .build();

        } catch (Exception e) {
            log.error("[WebClothesExtractor] 웹스크래핑 실패: {}", url, e);
            ClothesExtractionFailedException exception = ClothesExtractionFailedException.withUrl(url);
            exception.initCause(e);
            throw exception;
        }
    }
}