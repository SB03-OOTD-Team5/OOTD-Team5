package com.sprint.ootd5team.domain.clothattribute.service;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefCreateRequest;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothAttribute;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothAttributeDefs;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothAttributeRepository;
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

	@Override
	public ClothAttribute create(ClothesAttributeDefCreateRequest request) {
		log.debug("의상 속성 생성 시작.");
		String name = request.name();
		List<ClothAttributeDefs> defs = request.selectableValues();

		ClothAttribute createdAttribute = new ClothAttribute(name,defs);

		clothAttributeRepository.save(createdAttribute);
		log.info("의상 속성 생성됨 : 속성명={},속성값={}", name, createdAttribute);
		return createdAttribute;
	}

	@Override
	public List<ClothAttribute> findAll() {
		log.debug("의상 속성 목록 조회 시작.");
		return clothAttributeRepository.findAll();
	}

	@Override
	public ClothAttribute find(UUID id) {
		log.debug("의상 속성 단일조회 시작.");
		clothAttributeRepository.findById(id).orElseThrow(IllegalArgumentException::new);
		return null;
	}
}
