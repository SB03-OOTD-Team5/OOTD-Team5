package com.sprint.ootd5team.domain.clothes.controller;

import com.sprint.ootd5team.domain.clothes.controller.api.ClothesApi;
import com.sprint.ootd5team.domain.clothes.dto.request.ClothesCreateRequest;
import com.sprint.ootd5team.domain.clothes.dto.request.ClothesUpdateRequest;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothes.extractor.ClothesExtractionService;
import com.sprint.ootd5team.domain.clothes.service.ClothesService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/clothes")
@RequiredArgsConstructor
@Slf4j
public class ClothesController implements ClothesApi {

    private final ClothesService clothesService;
    private final ClothesExtractionService clothesExtractionService;

    @Override
    public ResponseEntity<ClothesDtoCursorResponse> getClothes(
        UUID ownerId,
        ClothesType type,
        Instant cursor,
        UUID idAfter,
        int limit,
        Sort.Direction sortDirection
    ) {
        log.info("[ClothesController] 전체 조회 요청 수신: "
                + "ownerId={}, typeEqual={}, cursor={}, idAfter={}, limit={}, sort={}",
            ownerId, type, cursor, idAfter, limit, sortDirection.name());

        ClothesDtoCursorResponse response =
            clothesService.getClothes(ownerId, type, cursor, idAfter, limit, sortDirection);

        log.info("[ClothesController] 전체 조회 응답 반환: "
                + "data.size={}, hasNext={}, nextCursor={}, nextIdAfter={}",
            response.data().size(), response.hasNext(),
            response.nextCursor(), response.nextIdAfter());

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }

    @Override
    public ResponseEntity<ClothesDto> createClothes(ClothesCreateRequest request,
        MultipartFile image) {
        log.info("[ClothesController] 생성 요청 수신: hasImage={}, filename={}", image != null,
            (image != null ? image.getOriginalFilename() : null));
        ClothesDto clothesDto = clothesService.create(request, image);

        log.info("[ClothesController] 생성 응답 반환: name={}, image={}", clothesDto.name(),
            clothesDto.imageUrl());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(clothesDto);
    }

    @Override
    public ResponseEntity<Void> deleteClothes(UUID clothesId) {
        log.info("[ClothesController] 삭제 요청 수신: clothesId={}", clothesId);
        clothesService.delete(clothesId);

        log.info("[ClothesController] 삭제 응답 완료");

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ClothesDto> updateClothes(UUID clothesId, ClothesUpdateRequest request,
        MultipartFile image) {
        log.info("[ClothesController] 수정 요청 수신: clothesId={}, request={}, image={}", clothesId,
            request, image);
        ClothesDto clothesDto = clothesService.update(clothesId, request, image);

        log.info("[ClothesController] 수정 응답 반환: name={}, image={}", clothesDto.name(),
            clothesDto.imageUrl());
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(clothesDto);
    }

    /**
     * 의상 정보 추출 API.
     *
     * <p>외부 URL을 입력받아 해당 페이지에서 의상 정보를 크롤링한다.
     * <br>요청 시 기본 검증:
     * <ul>
     *   <li>null/빈 문자열 차단</li>
     *   <li>스킴은 http/https만 허용</li>
     *   <li>잘못된 URI 형식은 400 Bad Request 응답</li>
     * </ul>
     *
     * @param url 의상 페이지 URL
     * @return 추출된 의상 정보 DTO (성공 시 200 OK, 잘못된 입력 시 400 Bad Request)
     */
    @Override
    public ResponseEntity<ClothesDto> extractByUrl(String url) {
        log.info("[ClothesController] extractByUrl 요청 url={}", url);
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            var u = java.net.URI.create(url);
            String scheme = u.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase(
                "https"))) {
                return ResponseEntity.badRequest().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        ClothesDto clothesDto = clothesExtractionService.extractByUrl(url);

        log.info("[ClothesController] 추출 결과 name={}, imageUrl={}",
            clothesDto.name(), clothesDto.imageUrl());
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(clothesDto);
    }
}
