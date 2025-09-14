package com.sprint.ootd5team.domain.clothes.controller;

import com.sprint.ootd5team.domain.clothes.controller.api.ClothesApi;
import com.sprint.ootd5team.domain.clothes.dto.request.ClothesCreateRequest;
import com.sprint.ootd5team.domain.clothes.dto.request.ClothesUpdateRequest;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothes.service.ClothesService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public ResponseEntity<ClothesDtoCursorResponse> getClothes(
        UUID ownerId,
        ClothesType type,
        String cursor,
        UUID idAfter,
        int limit
    ) {
        log.info("[ClothesController] 요청 수신: ownerId={}, typeEqual={}, cursor={}, idAfter={}, limit={}",
            ownerId, type, cursor, idAfter, limit);

        ClothesDtoCursorResponse response =
            clothesService.getClothes(ownerId, type, cursor, idAfter, limit);

        log.info("[ClothesController] 응답 반환: data.size={}, hasNext={}, nextCursor={}, nextIdAfter={}",
            response.data().size(), response.hasNext(),
            response.nextCursor(), response.nextIdAfter());

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }

    @Override
    public ResponseEntity<ClothesDto> createClothes(ClothesCreateRequest request,
        MultipartFile image) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteClothes(UUID clothesId) {
        return null;
    }

    @Override
    public ResponseEntity<ClothesDto> updateClothes(UUID clothesId, ClothesUpdateRequest request,
        MultipartFile image) {
        return null;
    }

    @Override
    public ResponseEntity<ClothesDto> extractByUrl(String url) {
        return null;
    }
}
