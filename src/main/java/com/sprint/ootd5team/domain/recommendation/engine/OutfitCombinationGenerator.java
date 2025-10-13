package com.sprint.ootd5team.domain.recommendation.engine;

import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.engine.model.OutfitScore;
import com.sprint.ootd5team.domain.recommendation.engine.model.ScoredClothes;
import java.util.ArrayList;
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
 * ----------------------------
 * - 필수 아이템: 상의/하의 or 원피스 + 신발
 * - 선택 아이템: 아우터, 악세사리, 언더웨어, 기타 등 (점수 기반 추가)
 * - 단품 점수(기본) + 조합 점수(OutfitCombinationEngine) 를 합산해 최종 코디 점수 산출
 * - 상위 N개 코디만 유지하여 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutfitCombinationGenerator {

    private static final int MAX_TOP_OUTFITS = 10;              // 상위 최대 N개 조합만 유지
    private static final int MAX_OPTIONAL_ITEMS_PER_OUTFIT = 5;      // optional 최대 포함 수
    private static final int MAX_OPTIONAL_ITEMS_PER_TYPE = 5;      // optional 타입별 후보군 최대 포함 수
    private static final int MAX_DUPLICATE_ALLOWED_PER_TYPE = 3;      // 여러 개 허용 되는 타입 최대 수

    // 여러 개 착용 가능 타입
    private static final Set<ClothesType> MULTI_ALLOWED_TYPES = Set.of(
        ClothesType.ACCESSORY,
        ClothesType.UNDERWEAR,
        ClothesType.ETC
    );

    private final OutfitCombinationEngine combinationEngine;

    /**
     * 단품 후보들을 받아 조합 생성 + 점수 기반 Top-N 코디 선정
     */
    public List<OutfitScore> generateWithScoring(List<ScoredClothes> candidates) {
        log.debug("[OutfitCombinationGenerator] 코디 조합 생성 시작: 입력 의상 수 = {}", candidates.size());

        // 필수 카테고리 분류
        List<ScoredClothes> tops = filterByType(candidates, ClothesType.TOP);
        List<ScoredClothes> bottoms = filterByType(candidates, ClothesType.BOTTOM);
        List<ScoredClothes> dresses = filterByType(candidates, ClothesType.DRESS);

        // 선택(optional) 아이템 필터링 및 점수순 정렬
        Map<ClothesType, List<ScoredClothes>> optionalByType = candidates.stream()
            .filter(c -> !Set.of(ClothesType.TOP, ClothesType.BOTTOM, ClothesType.DRESS)
                .contains(c.item().type()))
            .collect(Collectors.groupingBy(c -> c.item().type()));

        // 타입 별 상위 5개 추출
        List<ScoredClothes> optionalCandidates = optionalByType.values().stream()
            .flatMap(typeList -> typeList.stream()
                .sorted(Comparator.comparingDouble(ScoredClothes::score).reversed())
                .limit(MAX_OPTIONAL_ITEMS_PER_TYPE))
            .toList();

        log.debug(
            "[OutfitCombinationGenerator] 카테고리 분류 결과: TOP={}, BOTTOM={}, DRESS={}, optional={}\n",
            tops.size(), bottoms.size(), dresses.size(), optionalCandidates.size());

        // 최소 힙으로 Top N개만 유지
        PriorityQueue<OutfitScore> topCombinations = new PriorityQueue<>(
            Comparator.comparingDouble(OutfitScore::score));

        // 상의 + 하의 조합
        for (ScoredClothes top : tops) {
            for (ScoredClothes bottom : bottoms) {
                addCombination(topCombinations, List.of(top.item(), bottom.item()),
                    optionalCandidates);
            }
        }

        // 원피스 조합
        for (ScoredClothes dress : dresses) {
            addCombination(topCombinations, List.of(dress.item()), optionalCandidates);
        }

        // 점수 순 정렬
        List<OutfitScore> sortedCombinations = topCombinations.stream()
            .sorted(Comparator.comparingDouble(OutfitScore::score).reversed())
            .toList();

        // 중복 제거 (의상 ID 기준)
        List<OutfitScore> uniqueCombinations = removeDuplicateCombinations(sortedCombinations);

        // 상위 3개 요약 출력
        logTopCombinations(uniqueCombinations, 3);

        return uniqueCombinations;
    }

    /**
     * 코디 조합 1개를 생성하고 점수 계산 후 큐에 추가
     *
     * @param topCombinations    Top-N 조합을 유지할 우선순위 큐
     * @param baseItems          기본 조합 (상의+하의 or 원피스)
     * @param optionalCandidates 추가 선택 가능한 아이템 목록
     */
    private void addCombination(
        PriorityQueue<OutfitScore> topCombinations,
        List<ClothesFilteredDto> baseItems,
        List<ScoredClothes> optionalCandidates
    ) {
        // 조합 생성
        List<ClothesFilteredDto> outfitItems = buildOutfitCombination(baseItems,
            optionalCandidates);

        // 단품 평균 점수 계산
        double baseScore = calculateBaseAverageScore(outfitItems, optionalCandidates);

        // 조합 점수 계산
        double combinationScore = combinationEngine.calculateCombinationBonus(outfitItems);

        // 최종 가중 점수 (단품 40% + 조합 60%)
        double finalScore = baseScore * 0.4 + combinationScore * 0.6;

        log.debug("[OutfitCombinationGenerator] [addCombination] 기본={}개, optional 후={}개, 조합점수={}",
            baseItems.size(), outfitItems.size(), combinationScore);

        // Top-N 큐에 추가
        addToQueue(topCombinations, outfitItems, finalScore);
    }

    /**
     * 선택(optional) 아이템 포함하여 코디 조합 생성
     */
    private List<ClothesFilteredDto> buildOutfitCombination(
        List<ClothesFilteredDto> baseItems,
        List<ScoredClothes> optionalItems
    ) {
        List<ClothesFilteredDto> outfitItems = new ArrayList<>(baseItems);
        Map<ClothesType, Integer> typeCountMap = new HashMap<>();

        for (ScoredClothes candidate : optionalItems) {
            ClothesType type = candidate.item().type();
            int currentCount = typeCountMap.getOrDefault(type, 0);

            boolean canAdd =
                (MULTI_ALLOWED_TYPES.contains(type)
                    && currentCount < MAX_DUPLICATE_ALLOWED_PER_TYPE) ||
                    (!MULTI_ALLOWED_TYPES.contains(type) && currentCount < 1);

            if (canAdd) {
                outfitItems.add(candidate.item());
                typeCountMap.put(type, currentCount + 1);
            }

            // optional 개수 제한
            if (outfitItems.size() - baseItems.size() >= MAX_OPTIONAL_ITEMS_PER_OUTFIT) {
                break;
            }
        }

        return outfitItems;
    }

    /**
     * 단품 점수들의 평균 계산
     */
    private double calculateBaseAverageScore(
        List<ClothesFilteredDto> outfitItems,
        List<ScoredClothes> candidates
    ) {
        return outfitItems.stream()
            .mapToDouble(item -> candidates.stream()
                .filter(sc -> sc.item().clothesId().equals(item.clothesId()))
                .mapToDouble(ScoredClothes::score)
                .findFirst()
                .orElse(0))
            .average()
            .orElse(0);
    }

    /**
     * Top-N 큐 유지 (가장 낮은 점수 제거)
     */
    private void addToQueue(
        PriorityQueue<OutfitScore> queue,
        List<ClothesFilteredDto> items,
        double score
    ) {
        queue.add(new OutfitScore(items, score));
        if (queue.size() > MAX_TOP_OUTFITS) {
            queue.poll();
        }
    }

    /**
     * 동일 의상 조합(중복) 제거
     */
    private List<OutfitScore> removeDuplicateCombinations(List<OutfitScore> combinations) {
        List<OutfitScore> unique = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (OutfitScore combo : combinations) {
            String key = combo.outfit().stream()
                .map(o -> o.clothesId().toString())
                .sorted()
                .reduce((a, b) -> a + "-" + b)
                .orElse("");

            if (seen.add(key)) {
                unique.add(combo);
            } else {
                log.debug("[OutfitCombinationGenerator] 중복 조합 스킵: {}", key);
            }
        }
        return unique;
    }

    /**
     * 상위 N개 코디 요약 로그 출력
     */
    private void logTopCombinations(List<OutfitScore> combinations, int limit) {
        List<OutfitScore> top = combinations.stream().limit(limit).toList();

        String summary = top.stream()
            .map(c -> String.format("%.1f점 → %s",
                c.score(),
                c.outfit().stream()
                    .map(o -> o.name() + "(" + o.type() + ")")
                    .collect(Collectors.joining(", "))))
            .collect(Collectors.joining("\n"));

        log.info("""
            [OutfitCombinationGenerator] Summary: 상위 {}개 코디 추천 결과
            =====================================
            {}
            =====================================
            """, limit, summary);
    }

    /**
     * 특정 타입의 의상만 필터링
     */
    private List<ScoredClothes> filterByType(List<ScoredClothes> all, ClothesType type) {
        return all.stream()
            .filter(c -> c.item().type() == type)
            .collect(Collectors.toList());
    }
}