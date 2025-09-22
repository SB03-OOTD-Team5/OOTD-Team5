package com.sprint.ootd5team.domain.clothes.controller.api;

import com.sprint.ootd5team.domain.clothes.dto.request.ClothesCreateRequest;
import com.sprint.ootd5team.domain.clothes.dto.request.ClothesUpdateRequest;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "의상 관리", description = "의상 관련 API")
public interface ClothesApi {

    @Operation(
        summary = "옷 목록 조회",
        description = "조건에 맞는 옷 목록을 조회합니다.",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "옷 목록 조회 성공",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ClothesDtoCursorResponse.class))),
        @ApiResponse(responseCode = "400", description = "옷 목록 조회 실패",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    ResponseEntity<ClothesDtoCursorResponse> getClothes(
        @Parameter(description = "의상 소유자 ID") @RequestParam UUID ownerId,
        @Parameter(description = "의상 타입 필터") @RequestParam(name = "typeEqual", required = false) ClothesType type,
        @Parameter(description = "커서") @RequestParam(required = false) String cursor,
        @Parameter(description = "보조 커서(UUID)") @RequestParam(required = false) UUID idAfter,
        @Parameter(description = "페이지 크기", example = "20") @RequestParam(name = "limit", defaultValue = "20") int limit
    );

    @Operation(
        summary = "옷 등록",
        description = "새로운 옷을 등록합니다.",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "옷 등록 성공",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ClothesDto.class))),
        @ApiResponse(responseCode = "400", description = "옷 등록 실패",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ClothesDto> createClothes(
        @Parameter(description = "옷 등록 요청 DTO")
        @Valid @RequestPart("request") ClothesCreateRequest request,

        @Parameter(description = "옷 이미지 파일",
            schema = @Schema(type = "string", format = "binary"))
        @RequestPart(value = "image", required = false) MultipartFile image
    );

    @Operation(
        summary = "옷 삭제",
        description = "특정 의상을 삭제합니다.",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "옷 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "옷 삭제 실패",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{clothesId}")
    ResponseEntity<Void> deleteClothes(
        @Parameter(description = "의상 ID") @PathVariable UUID clothesId
    );

    @Operation(
        summary = "옷 수정",
        description = "기존 의상 정보를 수정합니다.",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "옷 수정 성공",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ClothesDto.class))),
        @ApiResponse(responseCode = "400", description = "옷 수정 실패",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping(value = "/{clothesId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ClothesDto> updateClothes(
        @Parameter(description = "의상 ID") @PathVariable UUID clothesId,

        @Parameter(description = "옷 수정 요청 DTO")
        @Valid @RequestPart("request") ClothesUpdateRequest request,

        @Parameter(description = "새 이미지 파일",
            schema = @Schema(type = "string", format = "binary"))
        @RequestPart(value = "image", required = false) MultipartFile image
    );

    @Operation(
        summary = "구매 링크로 옷 정보 불러오기",
        description = "구매 링크(URL)를 입력받아 의상 정보를 추출합니다.",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "옷 정보 추출 성공",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ClothesDto.class))),
        @ApiResponse(responseCode = "400", description = "옷 정보 추출 실패",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/extractions")
    ResponseEntity<ClothesDto> extractByUrl(
        @Parameter(description = "구매 링크 URL", example = "https://shop.example.com/product/123")
        @RequestParam String url
    );
}
