package com.sprint.ootd5team.domain.clothes.service;

import static org.springframework.util.StringUtils.hasText;

import com.sprint.ootd5team.base.exception.clothes.ClothesNotFoundException;
import com.sprint.ootd5team.base.exception.clothes.ClothesSaveFailedException;
import com.sprint.ootd5team.base.exception.clothesattribute.AttributeNotFoundException;
import com.sprint.ootd5team.base.exception.clothesattribute.AttributeValueNotAllowedException;
import com.sprint.ootd5team.base.exception.file.FileSaveFailedException;
import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.base.storage.FileStorage;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDto;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final AuthService authService;

    @Value("${ootd.storage.s3.prefix.clothes}")
    private String clothesPrefix;

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
        List<ClothesAttributeValue> cavs = toClothesAttributeValues(request.attributes());

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
            deleteFileSafely(imageUrl, "의상 저장 실패 롤백");
            throw ClothesSaveFailedException.withoutId(e);
        }
    }

    private void validateAttributeValue(ClothesAttribute attribute, String value) {
        boolean allowed = attribute.getDefs().stream()
            .anyMatch(def -> def.getAttDef().equals(value));
        if (!allowed) {
            throw AttributeValueNotAllowedException.withValue(attribute.getId(), value);
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
                image.getContentType(),
                clothesPrefix
            );
        } catch (IOException e) {
            log.warn("[clothes] 이미지 업로드 실패 {}", e.getMessage());
            throw FileSaveFailedException.withFileName(image.getOriginalFilename());
        }
    }

    /**
     * 의상 정보 수정
     *
     * 이미지: 새 파일 업로드 후 기존 파일 삭제
     * 속성: 요청된 속성과 현재 속성을 비교하여 추가/수정/삭제
     */
    @Transactional
    @Override
    public ClothesDto update(UUID clothesId, ClothesUpdateRequest request, MultipartFile image) {
        Clothes clothes = clothesRepository.findById(clothesId)
            .orElseThrow(() -> ClothesNotFoundException.withId(clothesId));

        UUID ownerId = clothes.getOwner().getId();
        log.info("[clothes] 수정 요청 - clothesId: {}, ownerId: {}", clothesId, ownerId);

        String newName = request.name();
        ClothesType newType = request.type();
        List<ClothesAttributeDto> newAttributes = request.attributes();

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
            String newKey = uploadClothesImage(image);  // 새 이미지 업로드
            String oldKey = clothes.getImageUrl();

            try {
                clothes.updateClothesImageUrl(newKey);
                deleteFileSafely(oldKey, "의상 이미지 교체");
            } catch (Exception e) {
                deleteFileSafely(newKey, "의상 수정 롤백");
                throw e;
            }
        }

        // 속성 수정
        if (newAttributes != null) {
            applyAttributes(clothes, request.attributes());
        }

        log.info("[clothes] 수정 완료 - clothesId: {}, name: {}, type: {}, imageUrl: {}",
            clothesId, clothes.getName(), clothes.getType(), clothes.getImageUrl());
        return clothesMapper.toDto(clothes);
    }

    /**
     * 의상의 속성 리스트에 요청된 속성 목록과 동기화
     * 추가: 요청에 있고, 현재 의상 속성에 없으면 추가
     * 수정: 동일 속성이 존재하지만, 값이 다르면 새 값으로 교체
     * 삭제: 현재 의상에 있지만, 요청에 없으면 제거
     * (현재 프론트에선 기존 의상 생성시 선택한 속성에 대해서 제거할 수 없다(기존값 유지 or 수정)
     */
    private void applyAttributes(Clothes clothes, List<ClothesAttributeDto> newAttributes) {
        Map<UUID, ClothesAttributeValue> currentMap = clothes.getClothesAttributeValues().stream()
            .collect(Collectors.toMap(cav -> cav.getAttribute().getId(), cav -> cav));

        Set<UUID> requestIds = newAttributes.stream()
            .map(ClothesAttributeDto::definitionId)
            .collect(Collectors.toSet());

        for (ClothesAttributeDto dto : newAttributes) {
            ClothesAttributeValue newCav = toClothesAttributeValue(dto);
            ClothesAttributeValue existing = currentMap.get(dto.definitionId());

            if (existing == null) {
                clothes.addClothesAttributeValue(newCav);
            } else if (!existing.getDefValue().equals(newCav.getDefValue())) {
                clothes.updateClothesAttributeValue(existing, newCav.getDefValue());
            }
        }

        // 요청에 없는 속성 제거
        clothes.getClothesAttributeValues().removeIf(
            cav -> !requestIds.contains(cav.getAttribute().getId())
        );
    }

    /**
     * ClothesAttributeDto → ClothesAttributeValue 변환 (검증 포함)
     */
    private ClothesAttributeValue toClothesAttributeValue(ClothesAttributeDto dto) {
        ClothesAttribute attr = attributeRepository.findById(dto.definitionId())
            .orElseThrow(() -> AttributeNotFoundException.withId(dto.definitionId()));

        validateAttributeValue(attr, dto.value());
        return new ClothesAttributeValue(attr, dto.value());
    }

    /**
     * ClothesAttributeDto 리스트 → ClothesAttributeValue 리스트 변환
     */
    private List<ClothesAttributeValue> toClothesAttributeValues(List<ClothesAttributeDto> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream()
            .map(this::toClothesAttributeValue)
            .collect(Collectors.toList());
    }

    /**
     * 파일 스토리지에서 안전하게 파일을 삭제한다.
     * <p>
     * 처리 정책:
     * <ul>
     *   <li>파일이 null이면 아무 작업도 하지 않는다.</li>
     *   <li>삭제 시도 중 예외가 발생해도 예외를 다시 던지지 않고 경고 로그만 남긴다.</li>
     *   <li>따라서 DB 트랜잭션은 영향을 받지 않으며,
     *       삭제 실패 시 고아 파일이 남을 수 있다.</li>
     *   <li>필요 시 배치 작업이나 로그 기반 모니터링으로 후처리 가능하다.</li>
     * </ul>
     *
     * @param key    삭제할 파일의 key (null 허용)
     * @param reason 삭제 사유 (로그 용도)
     */
    private void deleteFileSafely(String key, String reason) {
        if (!hasText(key)) return;
        try {
            fileStorage.delete(key);
            log.info("[clothes] 파일 삭제 성공 - key={}, reason={}", key, reason);
        } catch (Exception e) {
            log.warn("[clothes] 파일 삭제 실패 - key={}, reason={}", key, reason, e);
        }
    }

    /**
     * 의상 삭제
     * 이미지 파일 삭제는 {@link #deleteFileSafely(String, String)}에서 처리
     *
     */
    @Transactional
    @Override
    public void delete(UUID clothesId) {
        UUID currentUserId = authService.getCurrentUserId();

        Clothes clothes = clothesRepository.findById(clothesId)
            .orElseThrow(() -> {
                log.warn("[clothes] 삭제 실패 - 존재하지 않는 clothesId: {}", clothesId);
                throw ClothesNotFoundException.withId(clothesId);
            });

        if (!clothes.getOwner().getId().equals(currentUserId)) {
            throw new SecurityException();
        }

        deleteFileSafely(clothes.getImageUrl(), "의상 삭제");
        clothesRepository.deleteById(clothesId);
        log.info("[clothes] 삭제 완료 - ownerId={}", currentUserId);
    }

}
