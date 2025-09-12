package com.sprint.ootd5team.domain.clothattribute.controller;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefCreateRequest;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefDto;
import com.sprint.ootd5team.domain.clothattribute.service.ClothAttributeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/clothes/attribute-defs")
@RequiredArgsConstructor
public class ClothesAttributeController {
	private final ClothAttributeService clothAttributeService;

	/** 속성 등록
	 * 입력:
	 * ClothesAttributeDefCreateRequest
	 */
	@PostMapping
	public ResponseEntity<ClothesAttributeDefDto> register(
			@RequestBody ClothesAttributeDefCreateRequest request){
		ClothesAttributeDefDto result = clothAttributeService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}

	/**
	 * 의상 속성 정의 목록 조회
	 */
	@GetMapping
	public ResponseEntity<List<ClothesAttributeDefDto>> list(
		@RequestParam(defaultValue = "createdAt") String sortBy,
		@RequestParam(defaultValue = "ASCENDING") String sortDirection,
		@RequestParam(required = false) String keywordLike) {

		return ResponseEntity.ok(
			clothAttributeService.findAll(sortBy, sortDirection, keywordLike)
		);
	}
}
