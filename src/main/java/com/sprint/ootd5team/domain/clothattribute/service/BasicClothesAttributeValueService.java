package com.sprint.ootd5team.domain.clothattribute.service;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeValue;
import com.sprint.ootd5team.domain.clothattribute.mapper.ClothesAttributeMapper;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothesAttributeRepository;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothesAttributeValueRepository;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BasicClothesAttributeValueService implements ClothesAttributeValueService {

	private final ClothesAttributeValueRepository cavRepository;
	private final ClothesRepository clothesRepository;
	private final ClothesAttributeRepository attributeRepository;
	private final ClothesAttributeMapper mapper;

	@Override
	public ClothesAttributeWithDefDto create(UUID clothesId, UUID attributeId, String value) {
		Clothes clothes = clothesRepository.findById(clothesId)
			.orElseThrow(() -> new IllegalArgumentException("옷을 찾을 수 없음: " + clothesId));

		ClothesAttribute attribute = attributeRepository.findById(attributeId)
			.orElseThrow(() -> new IllegalArgumentException("속성을 찾을 수 없음: " + attributeId));

		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("속성 값은 비어 있을 수 없습니다.");
		}

		// 선택 가능한 하위값(Def) 검사
		boolean allowed = attribute.getDefs().stream()
			.anyMatch(def -> def.getAttDef().equals(value));
		if (!allowed) {
			throw new IllegalArgumentException("허용되지 않은 속성값입니다: " + value);
		}
		// (clothes_id, attributes_id) 유니크 → 있으면 갱신, 없으면 신규
		ClothesAttributeValue cav = cavRepository
			.findByClothesIdAndAttributeId(clothesId, attributeId)
			.orElse(new ClothesAttributeValue(clothes, attribute, value));

		cav.setDefValue(value);

		try {
			ClothesAttributeValue saved = cavRepository.save(cav);
			return mapper.toDto(saved);
		} catch (DataIntegrityViolationException e) {
			// 드물게 동시성 경합 시 유니크 제약 충돌 보호 (선택)
			log.warn("동시성으로 인한 유니크 충돌 재시도: clothesId={}, attributeId={}", clothesId, attributeId);
			ClothesAttributeValue existed = cavRepository
				.findByClothesIdAndAttributeId(clothesId, attributeId)
				.orElseThrow(() -> e);
			existed.setDefValue(value);
			return mapper.toDto(cavRepository.save(existed));
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<ClothesAttributeWithDefDto> getByClothesId(UUID clothesId) {
		List<ClothesAttributeValue> values = cavRepository.findAllByClothesId(clothesId);
		return values.stream()
			.map(mapper::toDto)
			.toList();
	}
}
