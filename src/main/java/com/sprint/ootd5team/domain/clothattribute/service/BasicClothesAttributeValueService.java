package com.sprint.ootd5team.domain.clothattribute.service;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothAttribute;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothAttributeValue;
import com.sprint.ootd5team.domain.clothattribute.mapper.ClothesAttributeMapper;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothAttributeRepository;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothesAttributeValueRepository;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import java.util.List;
import java.util.Optional;
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
	private final ClothesRepository clothesRepository;
	private final ClothAttributeRepository attributeRepository;
	private final ClothesAttributeMapper mapper;

	@Override
	public ClothesAttributeWithDefDto create(UUID clothesId, UUID attributeId, String value) {
		Clothes clothes = clothesRepository.findById(clothesId)
			.orElseThrow(() -> new IllegalArgumentException("옷을 찾을 수 없음: " + clothesId));

		ClothAttribute attribute = attributeRepository.findById(attributeId)
			.orElseThrow(() -> new IllegalArgumentException("속성을 찾을 수 없음: " + attributeId));

		// 기존 값 존재 여부 확인
		ClothAttributeValue cav = cavRepository.findByClothesIdAndAttributeId(clothesId, attributeId)
			.orElse(new ClothAttributeValue(clothes, attribute, value));

		// 이미 존재한다면 값만 갱신
		cav.setSelectableValue(value);

		ClothAttributeValue saved = cavRepository.save(cav);

		return mapper.toDto(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ClothesAttributeWithDefDto> getByClothesId(UUID clothesId) {
		List<ClothAttributeValue> values = cavRepository.findAllByClothesId(clothesId);
		return values.stream()
			.map(mapper::toDto)
			.toList();
	}
}
