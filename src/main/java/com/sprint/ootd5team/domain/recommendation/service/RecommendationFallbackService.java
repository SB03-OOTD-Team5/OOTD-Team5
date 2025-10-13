package com.sprint.ootd5team.domain.recommendation.service;

import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.mapper.RecommendationMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 필터링 실패 시 간단한 랜덤 코디 조합을 생성하는 서비스
 * <p>
 * - 상의 + 하의 조합
 * - 또는 원피스 조합
 * - 신발은 가능하면 포함
 * - optional 1~2개 추가
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationFallbackService {

    private static final Set<ClothesType> EXCLUDED_MAIN_TYPES = EnumSet.of(
        ClothesType.TOP,
        ClothesType.BOTTOM,
        ClothesType.DRESS,
        ClothesType.SHOES
    );
    // optional = 위 4개 타입을 제외한 나머지 모든 타입
    private static final Set<ClothesType> OPTIONAL_TYPES =
        EnumSet.complementOf(EnumSet.copyOf(EXCLUDED_MAIN_TYPES));
    private final ClothesRepository clothesRepository;
    private final RecommendationMapper recommendationMapper;
    private final Random random = new Random();

    public List<ClothesFilteredDto> getRandomOutfit(UUID userId) {
        List<Clothes> all = clothesRepository.findByOwner_Id(userId);
        if (all.isEmpty()) {
            log.warn("[Fallback] 사용자({})의 옷이 없습니다.", userId);
            return List.of();
        }

        List<Clothes> tops = filterByType(all, ClothesType.TOP);
        List<Clothes> bottoms = filterByType(all, ClothesType.BOTTOM);
        List<Clothes> dresses = filterByType(all, ClothesType.DRESS);
        List<Clothes> shoes = filterByType(all, ClothesType.SHOES);

        List<Clothes> selected = new ArrayList<>();
        boolean useDress = !dresses.isEmpty() && (tops.isEmpty() || random.nextBoolean());

        if (useDress) {
            selected.add(randomPick(dresses));
        } else if (!tops.isEmpty() && !bottoms.isEmpty()) {
            selected.add(randomPick(tops));
            selected.add(randomPick(bottoms));
        }

        if (!shoes.isEmpty()) {
            selected.add(randomPick(shoes));
        }

        // optional (나머지 모든 타입 중 일부 랜덤)
        List<Clothes> optionals = all.stream()
            .filter(c -> OPTIONAL_TYPES.contains(c.getType()) && !selected.contains(c))
            .toList();
        Collections.shuffle(optionals);
        int optionalCount = optionals.isEmpty() ? 0 : random.nextInt(2) + 1;
        selected.addAll(optionals.stream().limit(optionalCount).toList());

        log.debug("[Fallback] 랜덤 코디 생성 완료 ({}개)", selected.size());

        return selected.stream()
            .map(recommendationMapper::toFilteredDto)
            .toList();
    }

    private Clothes randomPick(List<Clothes> list) {
        return list.get(random.nextInt(list.size()));
    }

    private List<Clothes> filterByType(List<Clothes> all, ClothesType type) {
        return all.stream().filter(c -> c.getType() == type).toList();
    }
}
