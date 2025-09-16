package com.sprint.ootd5team.domain.clothes.service;

import static org.springframework.util.StringUtils.hasText;

import com.sprint.ootd5team.base.exception.clothes.ClothesNotFoundException;
import com.sprint.ootd5team.base.exception.file.FileSaveFailedException;
import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.base.storage.FileStorage;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDto;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeValue;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothesAttributeRepository;
import com.sprint.ootd5team.domain.clothes.dto.request.ClothesCreateRequest;
import com.sprint.ootd5team.domain.clothes.dto.request.ClothesUpdateRequest;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothes.mapper.ClothesMapper;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final FileStorage fileStorage;
    private final UserRepository userRepository;
    private final ClothesAttributeRepository attributeRepository;

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
    @Transactional(readOnly = true)
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

    /**
     * 새로운 의상을 등록
     *
     * @param request 의상 생성 요청 DTO (이름, 타입, 속성, 소유자 ID)
     * @param image   업로드할 의상 이미지 (null 가능)
     * @return 생성된 의상을 담은 ClothesDto
     * @throws UserNotFoundException   주어진 ownerId 에 해당하는 사용자가 없는 경우
     * @throws FileSaveFailedException 이미지 업로드에 실패한 경우
     */
    @Transactional
    @Override
    public ClothesDto create(ClothesCreateRequest request, MultipartFile image) {
        log.info("[ClothesService] 새 의상 등록 요청: ownerId={}, name={}, type={}, imagePresent={}",
            request.ownerId(), request.name(), request.type(), image != null && !image.isEmpty());

        // 1. 유저 확인
        UUID ownerId = request.ownerId();
        User owner = userRepository.findById(ownerId)
            .orElseThrow(() -> {
                log.error("[ClothesService] 존재하지 않는 사용자: ownerId={}", ownerId);
                return UserNotFoundException.withId(ownerId);
            });

        // 2. 속성 값 선행 검증 및 수집
        List<ClothesAttributeValue> cavs = new ArrayList<>();
        if (request.attributes() != null) {
            for (ClothesAttributeDto dto : request.attributes()) {
                ClothesAttribute attr = attributeRepository.findById(dto.definitionId())
                    .orElseThrow(() -> {
                        log.error("[ClothesService] 속성 정의 없음: definitionId={}", dto.definitionId());
                        throw new IllegalArgumentException("존재하지 않는 속성 정의");
                        //TODO: return ClothesAttributeNotFoundException.withId(dto.definitionId());
                    });

                validateAttributeValue(attr, dto.value());
                cavs.add(new ClothesAttributeValue(attr, dto.value()));
            }
        }

        // 3. 이미지 업로드
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = uploadClothesImage(image);
            log.debug("[ClothesService] 이미지 업로드 완료: url={}", imageUrl);
        }

        // 4. Clothes 엔티티 생성
        Clothes clothes = Clothes.builder()
            .owner(owner)
            .name(request.name())
            .type(request.type())
            .imageUrl(imageUrl)
            .build();

        cavs.forEach(clothes::addClothesAttributeValue);

        // 5. 저장 (실패 시 이미지 삭제)
        try {
            clothesRepository.save(clothes);
            log.info("[ClothesService] Clothes 저장 완료: clothesId={}, ownerId={}",
                clothes.getId(), ownerId);
            return clothesMapper.toDto(clothes);
        } catch (RuntimeException e) {
            if (imageUrl != null) {
                try {
                    fileStorage.delete(imageUrl);
                    log.warn("[ClothesService] 저장 실패로 업로드 파일 삭제: {}", imageUrl);
                } catch (Exception ex) {
                    log.warn("[ClothesService] 삭제 실패: url={}, cause={}", imageUrl, ex.toString());
                }
            }
            throw e;
        }
    }

    private void validateAttributeValue(ClothesAttribute attribute, String value) {
        boolean allowed = attribute.getDefs().stream()
            .anyMatch(def -> def.getAttDef().equals(value));
        if (!allowed) {
            // TODO: 커스텀 예외 (예: ClothesAttributeValueNotAllowedException)로 교체
            throw new IllegalArgumentException("허용되지 않은 속성값: " + value);
        }
    }

    /**
     * MultipartFile 형태의 이미지를 입력받아 파일 스토리지에 업로드
     *
     * @param image 업로드할 이미지 파일
     * @return 업로드된 이미지의 접근 가능 경로/url
     * @throws FileSaveFailedException 이미지 업로드 도중 I/O 오류가 발생한 경우
     */
    private String uploadClothesImage(MultipartFile image) {
        try (InputStream in = image.getInputStream()) {
            return fileStorage.upload(
                image.getOriginalFilename(),
                in,
                image.getContentType()
            );
        } catch (IOException e) {
            log.warn("[clothes] 이미지 업로드 실패 {}", e.getMessage());
            throw FileSaveFailedException.withFileName(image.getOriginalFilename());
        }
    }

    @Override
    public ClothesDto update(UUID clothesId, ClothesUpdateRequest request, MultipartFile image) {
        Clothes clothes = clothesRepository.findById(clothesId)
            .orElseThrow(() -> ClothesNotFoundException.withId(clothesId));

        UUID ownerId = clothes.getOwner().getId();
        log.info("[clothes] 수정 요청 - clothesId: {}, ownerId: {}", clothesId, ownerId);

        User user = userRepository.findById(ownerId)
            .orElseThrow(() -> {
                log.warn("[clothes] 수정 실패 - 존재하지 않는 userId: {}", ownerId);
                return new IllegalArgumentException("존재하지 않는 사용자");
            });

        String newName = request.name();
        ClothesType newType = request.type();
        List<ClothesAttributeWithDefDto> newAttributes = request.attributes();

        // 이름 수정
        if (hasText(newName) && !newName.equals(clothes.getName())) {
            clothes.updateClothesName(newName);
        }

        // 타입 수정
        if (newType != null && !newType.equals(clothes.getType())) {
            clothes.updateClothesType(newType);
        }

        // 이미지 수정
        if (image != null && !image.isEmpty()) {
            if (clothes.getImageUrl() != null) {
                fileStorage.delete(clothes.getImageUrl());
            }
            String uploadedUrl = uploadClothesImage(image);
            clothes.updateClothesImageUrl(uploadedUrl);
        }

//        // 속성 수정
//        if (request.attributes() != null) {
//            clothes.updateAttributes(request.attributes());
//        }

        log.info("[clothes] 수정 완료 - clothesId: {}, name: {}, type: {}, imageUrl: {}",
            clothesId, clothes.getName(), clothes.getType(), clothes.getImageUrl());
        return clothesMapper.toDto(clothes);

    }


}
