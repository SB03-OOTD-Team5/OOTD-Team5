package com.sprint.ootd5team.domain.clothes.service;

import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothes.mapper.ClothesMapper;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 의상 목록 조회 서비스 구현체
 * <p>
 * - QueryDSL 기반으로 옷 목록을 조회한다.
 * - 커서 기반 페이지네이션(cursor + idAfter)을 지원.
 * - 결과를 ClothesDtoCursorResponse 형태로 반환.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClothesServiceImpl implements ClothesService {

    private final ClothesRepository clothesRepository;
    private final ClothesMapper clothesMapper;

    /**
     * 특정 사용자의 의상 목록을 조회한다.
     *
     * @param ownerId 조회할 소유자 ID (UUID)
     * @param type    옷 종류 필터 (nullable)
     * @param cursor  커서 기준 createdAt (nullable)
     * @param idAfter 동일 createdAt 시 tie-breaker 용 UUID (nullable)
     * @param limit   조회할 데이터 개수
     * @return ClothesDtoCursorResponse (데이터, nextCursor, nextIdAfter, hasNext 등 포함)
     */
    @Override
    public ClothesDtoCursorResponse getClothes(
        UUID ownerId,
        ClothesType type,
        String cursor,
        UUID idAfter,
        int limit
    ) {
        log.info("[ClothesService] 옷 목록 조회 시작: ownerId={}, type={}, cursor={}, idAfter={}, limit={}",
            ownerId, type, cursor, idAfter, limit);

        Instant cursorInstant = cursor != null ? Instant.parse(cursor) : null;
        // 다음 페이지 여부 확인
        List<Clothes> result = clothesRepository.findClothes(
            ownerId, type, cursorInstant, idAfter, limit + 1
        );

        boolean hasNext = result.size() > limit;
        if (hasNext) {
            result = result.subList(0, limit);  // 초과분 제거
        }

        List<ClothesDto> dtoList = result.stream()
            .map(clothesMapper::toDto)
            .toList();

        String nextCursor = null;
        String nextIdAfter = null;
        if (hasNext && !result.isEmpty()) {
            Clothes last = result.get(result.size() - 1);
            nextCursor = last.getCreatedAt().toString();
            nextIdAfter = last.getId().toString();
            log.debug("[ClothesService] 다음 페이지 커서 계산: nextCursor={}, nextIdAfter={}",
                nextCursor, nextIdAfter);
        }

        ClothesDtoCursorResponse response = new ClothesDtoCursorResponse(
            dtoList,
            nextCursor,
            nextIdAfter,
            hasNext,
            (long) dtoList.size(),
            "createdAt",
            "DESC"
        );

        log.info("[ClothesService] 옷 목록 조회 완료: 반환 개수={}, hasNext={}",
            dtoList.size(), hasNext);

        return response;
    }
}
