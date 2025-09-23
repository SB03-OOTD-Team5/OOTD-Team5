package com.sprint.ootd5team.domain.clothes.service;

import com.sprint.ootd5team.domain.clothes.dto.request.ClothesCreateRequest;
import com.sprint.ootd5team.domain.clothes.dto.request.ClothesUpdateRequest;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

public interface ClothesService {

    ClothesDtoCursorResponse getClothes(
        UUID ownerId,
        ClothesType type,
        Instant cursor,
        UUID idAfter,
        int limit,
        Sort.Direction direction
    );

    ClothesDto create(ClothesCreateRequest request, MultipartFile image);

    ClothesDto update(UUID clothesId, ClothesUpdateRequest request, MultipartFile image);

    void delete(UUID ownerId, UUID clothesId);
}
