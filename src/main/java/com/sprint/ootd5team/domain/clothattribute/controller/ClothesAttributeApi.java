package com.sprint.ootd5team.domain.clothattribute.controller;

import com.sprint.ootd5team.base.exception.ErrorResponse;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefCreateRequest;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefDto;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "의상 속성 정의", description = "의상 속성 정의 관련 API")
@SecurityRequirement(name = "CustomHeaderAuth")
public interface ClothesAttributeApi {

	@Operation(summary = "의상 속성 정의 등록", description = "새로운 의상 속성 정의를 등록합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "등록 성공",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = ClothesAttributeDefDto.class))),
		@ApiResponse(responseCode = "400", description = "잘못된 요청",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	ClothesAttributeDefDto register(
		@Parameter(description = "의상 속성 정의 생성 요청")
		@Valid @RequestBody ClothesAttributeDefCreateRequest request
	);

	@Operation(summary = "의상 속성 정의 목록 조회", description = "조건에 맞는 의상 속성 정의 목록을 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
				array = @ArraySchema(schema = @Schema(implementation = ClothesAttributeDefDto.class)))),
		@ApiResponse(responseCode = "400", description = "잘못된 요청",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = ErrorResponse.class)))
	})
	@GetMapping
	List<ClothesAttributeDefDto> list(
		@Parameter(description = "정렬 대상 필드", example = "createdAt")
		@RequestParam(defaultValue = "createdAt") String sortBy,
		@Parameter(description = "정렬 방향", example = "ASCENDING")
		@RequestParam(defaultValue = "ASCENDING") String sortDirection,
		@Parameter(description = "속성명 검색 키워드")
		@RequestParam(required = false) String keywordLike
	);

	@Operation(summary = "의상 속성 정의 수정", description = "기존 의상 속성 정의를 수정합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "수정 성공",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = ClothesAttributeDefDto.class))),
		@ApiResponse(responseCode = "400", description = "잘못된 요청",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PatchMapping("/{definitionId}")
	ClothesAttributeDefDto update(
		@Parameter(description = "의상 속성 정의 ID") @PathVariable UUID id,
		@Parameter(description = "의상 속성 정의 수정 요청")
		@Valid @RequestBody ClothesAttributeDefUpdateRequest request
	);

	@Operation(summary = "의상 속성 정의 삭제", description = "의상 속성 정의를 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "삭제 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = ErrorResponse.class)))
	})
	@DeleteMapping("/{definitionId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(@Parameter(description = "의상 속성 정의 ID") @PathVariable UUID id);
}
