package com.sprint.ootd5team.domain.clothattribute.service;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeValue;
import com.sprint.ootd5team.domain.clothattribute.mapper.ClothesAttributeMapper;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothesAttributeRepository;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothesAttributeValueRepository;
//import com.sprint.ootd5team.domain.clothes.entity.Clothes;
//import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BasicClothesAttributeValueService implements ClothesAttributeValueService {

	private final ClothesAttributeValueRepository cavRepository;
//	private final ClothesRepository clothesRepository;
	private final ClothesAttributeRepository attributeRepository;
	private final ClothesAttributeMapper mapper;

//	@Override
//	public ClothesAttributeWithDefDto create(UUID clothesId, UUID attributeId, String value) {
//		Clothes clothes = clothesRepository.findById(clothesId)
//			.orElseThrow(() -> new IllegalArgumentException("옷을 찾을 수 없음: " + clothesId));
//
//		ClothAttribute attribute = attributeRepository.findById(attributeId)
//			.orElseThrow(() -> new IllegalArgumentException("속성을 찾을 수 없음: " + attributeId));
//
//		if (value == null || value.isBlank()) {
//			throw new IllegalArgumentException("속성 값은 비어 있을 수 없습니다.");
//		}
//
//		// 선택 가능한 하위값(Def) 검사
//		boolean allowed = attribute.getDefs().stream()
//		.anyMatch(def -> def.getValue().equals(value));
//		if (!allowed) {
//		throw new IllegalArgumentException("허용되지 않은 속성값입니다: " + value);
//		}
//		// 기존 값 존재 여부 확인
//		ClothAttributeValue cav = cavRepository.findByClothesIdAndAttributeId(clothesId, attributeId)
//			.orElse(new ClothAttributeValue(clothes, attribute, value));
//
//		// 이미 존재한다면 값만 갱신
//		cav.setSelectableValue(value);
//
//		ClothAttributeValue saved = cavRepository.save(cav);
//
//		return mapper.toDto(saved);
//	}

	@Override
	@Transactional(readOnly = true)
	public List<ClothesAttributeWithDefDto> getByClothesId(UUID clothesId) {
		List<ClothesAttributeValue> values = cavRepository.findAllByClothesId(clothesId);
		return values.stream()
			.map(mapper::toDto)
			.toList();
	}
}
