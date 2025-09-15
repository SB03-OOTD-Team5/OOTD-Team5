package com.sprint.ootd5team.domain.clothattribute.service;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefCreateRequest;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefDto;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDto;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeDef;
import com.sprint.ootd5team.domain.clothattribute.mapper.ClothesAttributeMapper;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothesAttributeRepository;
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
public class BasicClothesAttributeService implements ClothesAttributeService {

	private final ClothesAttributeRepository clothesAttributeRepository;
	private final ClothesAttributeMapper mapper;

	@Override
	@Transactional
	public ClothesAttributeDefDto create(ClothesAttributeDefCreateRequest request) {
		log.debug("의상 속성,하위속성 생성 시작.");

		// 속성 엔티티 생성(부모 엔티티)
		ClothesAttribute createdAttribute = new ClothesAttribute(request.name());

		// 하위속성 엔티티 목록 생성(자식 엔티티)
		List<ClothesAttributeDef> attributeDefs = request.selectableValues().stream()
			.map(def -> new ClothesAttributeDef(createdAttribute, def))
			.toList();

		// 속성-하위속성 연결
		createdAttribute.setDefs(attributeDefs);

		if (request.name() == null || request.name().isBlank()) {
			throw new IllegalArgumentException("속성명은 비어 있을 수 없습니다.");
		}
		if (clothesAttributeRepository.existsByNameIgnoreCase(request.name())) {
			throw new IllegalStateException("이미 존재하는 속성명입니다: " + request.name());
		}
		ClothesAttribute saved = clothesAttributeRepository.save(createdAttribute);
		log.info("의상 속성 생성됨 : 속성명={}, 하위속성 수={}", saved.getName(), saved.getDefs().size());
		return mapper.toDto(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ClothesAttributeDefDto> findAll(String sortBy, String sortDirection, String keywordLike) {
		List<ClothesAttributeDefDto> all = clothesAttributeRepository.findAll().stream()
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
		ClothesAttribute attribute = clothesAttributeRepository.findById(attributeId)
			.orElseThrow(() -> new IllegalArgumentException("속성을 찾을 수 없음: " + attributeId));

		// 자식 엔티티 → DTO 변환
		return attribute.getDefs().stream()
			.map(mapper::toDto)
			.toList();
	}
}
