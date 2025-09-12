package com.sprint.ootd5team.domain.clothattribute.service;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefCreateRequest;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefDto;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDto;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothAttribute;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothAttributeDef;
import com.sprint.ootd5team.domain.clothattribute.mapper.ClothesAttributeMapper;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothAttributeRepository;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class BasicClothAttributeService implements ClothAttributeService {

	private final ClothAttributeRepository clothAttributeRepository;
	private final ClothesAttributeMapper mapper;

	@Override
	@Transactional
	public ClothesAttributeDefDto create(ClothesAttributeDefCreateRequest request) {
		log.debug("의상 속성,하위속성 생성 시작.");

		// 속성 엔티티 생성(부모 엔티티)
		ClothAttribute createdAttribute = new ClothAttribute(request.name());

		// 하위속성 엔티티 목록 생성(자식 엔티티)
		List<ClothAttributeDef> attributeDefs = request.selectableValues().stream()
			.map(def -> new ClothAttributeDef(createdAttribute, def))
			.toList();

		// 속성-하위속성 연결
		createdAttribute.setDefs(attributeDefs);

		ClothAttribute saved = clothAttributeRepository.save(createdAttribute);
		log.info("의상 속성 생성됨 : 속성명={}, 하위속성 수={}", saved.getName(), saved.getDefs().size());
		return mapper.toDto(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ClothesAttributeDefDto> findAll(String sortBy, String sortDirection, String keywordLike) {
		List<ClothesAttributeDefDto> all = clothAttributeRepository.findAll().stream()
			.map(mapper::toDto)
			.toList();

		// 1. 검색
		if (keywordLike != null && !keywordLike.isBlank()) {
			all = all.stream()
				.filter(dto -> dto.name().contains(keywordLike))
				.toList();
		}

		// 2. 정렬
		Comparator<ClothesAttributeDefDto> comparator =
			"name".equalsIgnoreCase(sortBy)
				? Comparator.comparing(ClothesAttributeDefDto::name)
				: Comparator.comparing(ClothesAttributeDefDto::createdAt);

		if ("DESCENDING".equalsIgnoreCase(sortDirection)) {
			comparator = comparator.reversed();
		}

		return all.stream().sorted(comparator).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ClothesAttributeDto> getValuesByAttributeId(UUID attributeId) {
		ClothAttribute attribute = clothAttributeRepository.findById(attributeId)
			.orElseThrow(() -> new IllegalArgumentException("속성을 찾을 수 없음: " + attributeId));

		// 자식 엔티티 → DTO 변환
		return attribute.getDefs().stream()
			.map(mapper::toDto)
			.toList();
	}
}
