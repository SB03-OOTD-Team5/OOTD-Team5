package com.sprint.ootd5team.domain.clothattribute.controller;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefCreateRequest;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefDto;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefUpdateRequest;
import com.sprint.ootd5team.domain.clothattribute.service.ClothesAttributeService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/clothes/attribute-defs")
@RequiredArgsConstructor
public class ClothesAttributeController {
	private final ClothesAttributeService clothesAttributeService;

	/** 속성 등록
	 * 입력:
	 * ClothesAttributeDefCreateRequest
	 */
	@PostMapping
	public ResponseEntity<ClothesAttributeDefDto> register(
			@RequestBody ClothesAttributeDefCreateRequest request){
		ClothesAttributeDefDto result = clothesAttributeService.create(request);
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
			clothesAttributeService.findAll(sortBy, sortDirection, keywordLike)
		);
	}

	/**
	 * 의상 속성 정의 수정
	 */
	@PatchMapping("/{id}")
	public ResponseEntity<ClothesAttributeDefDto> update(
		@PathVariable UUID id, @Validated @RequestBody ClothesAttributeDefUpdateRequest request
	){
		ClothesAttributeDefDto result = clothesAttributeService.update(id,request);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	/**
	 * 의상 속성 정의 삭제
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
		@PathVariable UUID id){
		clothesAttributeService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
