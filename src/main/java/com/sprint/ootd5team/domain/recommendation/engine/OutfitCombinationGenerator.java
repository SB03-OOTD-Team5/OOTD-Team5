package com.sprint.ootd5team.domain.recommendation.engine;

import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationClothesDto;
import com.sprint.ootd5team.domain.recommendation.engine.model.OutfitScore;
import com.sprint.ootd5team.domain.recommendation.engine.model.ScoredClothes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 코디 조합 생성기
 * <p>
 * - 필수 아이템: 상의/하의 or 원피스 + 신발
 * - 선택 아이템: 아우터 + 악세사리/언더웨어/기타 (점수 기반 선택)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutfitCombinationGenerator {

    private static final int MAX_COMBINATIONS = 10;       // 상위 최대 N개 조합만 유지
    private static final int MAX_OPTIONAL_ITEMS = 5;      // optional 최대 포함 수
    private static final int MAX_OPTIONAL_MULTI_ITEMS = 3;      // 여러 개 허용 되는 타입 최대 수
    // 여러 개 허용되는 타입
    private static final Set<ClothesType> MULTI_ALLOWED_TYPES = Set.of(
        ClothesType.ACCESSORY,
        ClothesType.UNDERWEAR,
        ClothesType.ETC
    );
    private final OutfitCombinationEngine combinationEngine;

    /**
     * 단품 후보들을 받아 조합 생성 + 점수 기반 Top-N 코디 선정
     */
    public List<OutfitScore> generateWithScoring(List<ScoredClothes> clothes) {
        log.debug("[OutfitGenerator] 입력 의상 수 = {}", clothes.size());

        // 필수
        List<ScoredClothes> tops = filterByType(clothes, ClothesType.TOP);
        List<ScoredClothes> bottoms = filterByType(clothes, ClothesType.BOTTOM);
        List<ScoredClothes> dresses = filterByType(clothes, ClothesType.DRESS);

        // optional
        List<ScoredClothes> optionals = clothes.stream()
            .filter(c -> !Set.of(ClothesType.TOP, ClothesType.BOTTOM, ClothesType.DRESS)
                .contains(c.item().type()))
            .sorted(Comparator.comparingDouble(ScoredClothes::score).reversed())
            .toList();

        log.debug("[OutfitGenerator] TOP={}, BOTTOM={}, DRESS={}, optional={}",
            tops.size(), bottoms.size(), dresses.size(), optionals.size());

        // 최소 힙으로 Top N개만 유지
        PriorityQueue<OutfitScore> pq = new PriorityQueue<>(
            Comparator.comparingDouble(OutfitScore::score));

        // 상의 + 하의 조합
        for (ScoredClothes top : tops) {
            for (ScoredClothes bottom : bottoms) {
                addCombination(pq, List.of(top.item(), bottom.item()), optionals);
            }
        }

        // 원피스 조합
        for (ScoredClothes dress : dresses) {
            addCombination(pq, List.of(dress.item()), optionals);
        }

        // 결과 출력
        List<OutfitScore> result = pq.stream()
            .sorted(Comparator.comparingDouble(OutfitScore::score).reversed())
            .toList();
        List<OutfitScore> deduped = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();

        for (OutfitScore score : result) {
            String key = score.outfit().stream()
                .map(o -> o.clothesId().toString())
                .sorted()
                .reduce((a, b) -> a + "-" + b)
                .orElse("");

            if (seenKeys.add(key)) {
                deduped.add(score);
            } else {
                log.debug("[Generator] Duplicate outfit skipped: {}", key);
            }
        }

        log.debug("[OutfitGenerator] 최종 조합 수 = {}", result.size());
        if (!result.isEmpty()) {
            log.debug("[OutfitGenerator] 상위 3개 점수 미리보기: {}",
                result.stream().limit(3).map(OutfitScore::score).toList());
        }

        return deduped;
    }

    /**
     * optional 아이템 포함 + 점수 계산
     */
    private void addCombination(
        PriorityQueue<OutfitScore> pq,
        List<RecommendationClothesDto> base,
        List<ScoredClothes> optionals
    ) {
        // 선택 아이템 순서 무작위
        List<ScoredClothes> shuffled = new ArrayList<>(optionals);
        Collections.shuffle(shuffled);

        // 기본 조합 + 타입별 카운터
        List<RecommendationClothesDto> result = new ArrayList<>(base);
        Map<ClothesType, Integer> addedCount = new HashMap<>();

        for (ScoredClothes sc : optionals) {
            ClothesType type = sc.item().type();
            int current = addedCount.getOrDefault(type, 0);

            // 타입별 개수 제한
            boolean canAdd =
                (MULTI_ALLOWED_TYPES.contains(type) && current < MAX_OPTIONAL_MULTI_ITEMS) ||
                    (!MULTI_ALLOWED_TYPES.contains(type) && current < 1);

            if (canAdd) {
                result.add(sc.item());
                addedCount.put(type, current + 1);
            }

            // 전체 optional 수 제한
            if (result.size() - base.size() >= MAX_OPTIONAL_ITEMS) {
                break;
            }
        }

        double comboScore = combinationEngine.calculateCombinationBonus(result);
        pq.add(new OutfitScore(result, comboScore));

        if (pq.size() > MAX_COMBINATIONS) {
            pq.poll();
        }
    }

    private List<ScoredClothes> filterByType(List<ScoredClothes> all, ClothesType type) {
        return all.stream()
            .filter(c -> c.item().type() == type)
            .collect(Collectors.toList());
    }
}
