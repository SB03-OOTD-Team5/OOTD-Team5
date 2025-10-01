package com.sprint.ootd5team.domain.clothesattribute.controller;

import com.sprint.ootd5team.domain.clothesattribute.dto.ClothesAttributeDefCreateRequest;
import com.sprint.ootd5team.domain.clothesattribute.dto.ClothesAttributeDefDto;
import com.sprint.ootd5team.domain.clothesattribute.dto.ClothesAttributeDefUpdateRequest;
import com.sprint.ootd5team.domain.clothesattribute.service.ClothesAttributeService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/clothes/attribute-defs")
@RequiredArgsConstructor
@Validated
public class ClothesAttributeController implements ClothesAttributeApi {
	private final ClothesAttributeService clothesAttributeService;

	/** 속성 등록
	 * 입력:
	 * ClothesAttributeDefCreateRequest
	 */
	@Override
	public ClothesAttributeDefDto register(ClothesAttributeDefCreateRequest request){
		return clothesAttributeService.create(request);
	}

	/**
	 * 의상 속성 정의 목록 조회
	 */
	@Override
	public List<ClothesAttributeDefDto> list(
		String sortBy,
		String sortDirection,
		String keywordLike) {

		return clothesAttributeService.findAll(sortBy, sortDirection, keywordLike);
	}

	/**
	 * 의상 속성 정의 수정
	 */
	@Override
	public ClothesAttributeDefDto update(
		UUID id, ClothesAttributeDefUpdateRequest request
	){
		return clothesAttributeService.update(id,request);
	}
	/**
	 * 의상 속성 정의 삭제
	 */
	@Override
	public void delete(UUID id){
		clothesAttributeService.delete(id);
	}
}
