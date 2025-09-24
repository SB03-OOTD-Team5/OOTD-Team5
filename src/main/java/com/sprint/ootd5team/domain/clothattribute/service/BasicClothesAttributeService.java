package com.sprint.ootd5team.domain.clothattribute.service;

import com.sprint.ootd5team.base.exception.clothesattribute.AttributeAlreadyExistException;
import com.sprint.ootd5team.base.exception.clothesattribute.AttributeNotFoundException;
import com.sprint.ootd5team.base.exception.clothesattribute.InvalidAttributeException;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefCreateRequest;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefDto;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefUpdateRequest;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDto;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeDef;
import com.sprint.ootd5team.domain.clothattribute.mapper.ClothesAttributeMapper;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothesAttributeRepository;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothesAttributeValueRepository;
import com.sprint.ootd5team.domain.notification.event.type.ClothesAttributeCreateEvent;
import com.sprint.ootd5team.domain.notification.event.type.ClothesAttributeUpdateEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class BasicClothesAttributeService implements ClothesAttributeService {

    private final ClothesAttributeRepository clothesAttributeRepository;
    private final ClothesAttributeValueRepository clothesAttributeValueRepository;
    private final ClothesAttributeMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public ClothesAttributeDefDto create(ClothesAttributeDefCreateRequest request) {
        log.debug("[ClothesAttributeService] 의상 속성,하위속성 생성 시작.");

        String normalizedName = normalizeName(request.name());
        List<String> sanitizedValues = sanitizeSelectableValues(request.selectableValues());

        if (clothesAttributeRepository.existsByNameIgnoreCase(normalizedName)) {
            throw AttributeAlreadyExistException.withName(normalizedName);
        }

        ClothesAttribute createdAttribute = new ClothesAttribute(normalizedName);
        sanitizedValues.forEach(
            value -> createdAttribute.addDef(new ClothesAttributeDef(createdAttribute, value)));

        ClothesAttribute saved = clothesAttributeRepository.save(createdAttribute);
        log.info("[ClothesAttributeService] 의상 속성 생성됨 : 속성명={}, 하위속성 수={}", saved.getName(),
            saved.getDefs().size());

        ClothesAttributeDefDto dto = mapper.toDto(saved);

        // 알림 전송
        eventPublisher.publishEvent(new ClothesAttributeCreateEvent(dto));

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClothesAttributeDefDto> findAll(String sortBy, String sortDirection,
        String keywordLike) {
        log.debug("[ClothesAttributeService] 전체 속성&하위속성 검색,정렬조회 시작.");
        List<ClothesAttributeDefDto> all = clothesAttributeRepository.findAll().stream()
            .map(mapper::toDto)
            .toList();
        int attTotalSize = all.size();
        // 1. 검색
        if (keywordLike != null && !keywordLike.isBlank()) {
            all = all.stream()
                .filter(dto -> dto.name().contains(keywordLike))
                .toList();
        }

        // 2. 정렬
        Comparator<ClothesAttributeDefDto> comparator =
            "name".equalsIgnoreCase(sortBy)
                ? Comparator.comparing(ClothesAttributeDefDto::name)
                : Comparator.comparing(ClothesAttributeDefDto::createdAt);

        if ("DESCENDING".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }
        List<ClothesAttributeDefDto> result = all.stream().sorted(comparator).toList();
        log.info(
            "[ClothesAttributeService] 전체 속성&하위속성 검색,정렬조회 성공: 전체 속성 수= {}rows, 검색된 속성 수={}rows ",
            attTotalSize, result.size());
        return result;
    }

    @Override
    @Transactional
    public ClothesAttributeDefDto update(UUID id, ClothesAttributeDefUpdateRequest request) {
        log.debug("[ClothesAttributeService] 의상 속성,하위속성 수정 시작.");

        ClothesAttribute targetAttribute = clothesAttributeRepository.findById(id)
            .orElseThrow(AttributeNotFoundException::new);

        String newName = normalizeName(request.name());
        List<String> sanitizedValues = sanitizeSelectableValues(request.selectableValues());

        if (!targetAttribute.getName().equalsIgnoreCase(newName)
            && clothesAttributeRepository.existsByNameIgnoreCase(newName)) {
            throw AttributeAlreadyExistException.withName(newName);
        }

        List<ClothesAttributeDef> newDefs = sanitizedValues.stream()
            .map(value -> new ClothesAttributeDef(targetAttribute, value))
            .toList();

        // 새로운 값으로 갱신
        targetAttribute.clearDefs();
        em.flush();
        newDefs.forEach(targetAttribute::addDef);

        // 속성명 교체
        targetAttribute.rename(newName);

        // 영속화
        ClothesAttribute saved = clothesAttributeRepository.save(targetAttribute);
        log.info("[ClothesAttributeService] 의상 속성 수정됨 : 속성명={}, 하위속성 수={}", saved.getName(),
            saved.getDefs().size());

        ClothesAttributeDefDto dto = mapper.toDto(saved);

        // 알림 전송
        eventPublisher.publishEvent(new ClothesAttributeUpdateEvent(dto));

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClothesAttributeDto> getValuesByAttributeId(UUID attributeId) {
        ClothesAttribute attribute = clothesAttributeRepository.findById(attributeId)
            .orElseThrow(AttributeNotFoundException::new);

        // 자식 엔티티 → DTO 변환
        return attribute.getDefs().stream()
            .map(mapper::toDto)
            .toList();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.debug("[ClothesAttributeService] 의상 속성,하위속성 제거 시작.");

        ClothesAttribute targetAttribute = clothesAttributeRepository.findById(id)
            .orElseThrow(AttributeNotFoundException::new);

        if (clothesAttributeValueRepository.existsByAttribute_Id(id)) {
            throw new InvalidAttributeException();
        }

        //하위속성 제거
        targetAttribute.clearDefs();
        em.flush();
        // 속성 엔티티 제거
        String deletedName = targetAttribute.getName();
        clothesAttributeRepository.delete(targetAttribute);

        log.info("[ClothesAttributeService] 의상 속성 제거됨: 속성명={}", deletedName);
    }

    private String normalizeName(String rawName) {
        String normalized = rawName == null ? "" : rawName.trim();
        if (normalized.isBlank()) {
            throw new InvalidAttributeException();
        }
        return normalized;
    }

    private List<String> sanitizeSelectableValues(List<String> rawValues) {
        if (rawValues == null) {
            throw new InvalidAttributeException();
        }
        LinkedHashSet<String> sanitized = rawValues.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
        if (sanitized.isEmpty()) {
            throw new InvalidAttributeException();
        }
        return List.copyOf(sanitized);
    }
}
