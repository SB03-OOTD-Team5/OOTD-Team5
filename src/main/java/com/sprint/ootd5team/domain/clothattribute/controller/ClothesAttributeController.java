package com.sprint.ootd5team.domain.clothattribute.controller;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefCreateRequest;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothAttribute;
import com.sprint.ootd5team.domain.clothattribute.service.ClothAttributeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/clothes/attribute-defs")
@RequiredArgsConstructor
public class ClothesAttributeController {
	private final ClothAttributeService clothAttributeService;

	/** 속성 등록
	 * 입력 :
	 * ClothesAttributeDefCreateRequest
	 */
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ClothAttribute> register(ClothesAttributeDefCreateRequest request)
	{
		ClothAttribute saved = clothAttributeService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	@GetMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<List<ClothAttribute>> getAll()
	{
		List<ClothAttribute> result = clothAttributeService.findAll();
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}


}
