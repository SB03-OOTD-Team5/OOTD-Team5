package com.sprint.ootd5team.domain.recommendation.engine;

import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.recommendation.engine.model.ClothesScore;
import com.sprint.ootd5team.domain.recommendation.engine.model.OutfitScore;
import com.sprint.ootd5team.domain.recommendation.enums.ClothesStyle;
import com.sprint.ootd5team.domain.recommendation.enums.type.OuterType;
import com.sprint.ootd5team.domain.recommendation.enums.type.TopType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutfitCombinationGenerator {

    private static final int MAX_TOP_BOTTOM_BASE_SIZE = 10;  // 상의+하의 빔 폭
    private static final int MAX_DRESS_BASE_SIZE = 5;        // 원피스 빔 폭

    private static final double DUPLICATE_TIE_DELTA = 1;   // 점수 차이 임계값 ( n 이내면 랜덤 선택)

    public List<OutfitScore> generateWithScoring(List<ClothesScore> candidates) {
        // 1. 타입별 그룹 지정
        Map<ClothesType, List<ClothesScore>> grouped = candidates.stream()
            .collect(Collectors.groupingBy(c -> c.item().type()));

        // 2. 기본 조합 outfitScore에 추가 후 상위 n개 반환
        List<OutfitScore> topBottomBase = buildBaseCombinations(
            grouped.getOrDefault(ClothesType.TOP, List.of()),
            Optional.of(grouped.getOrDefault(ClothesType.BOTTOM, List.of()))
        );
        List<OutfitScore> dressBase = List.of();
        List<ClothesScore> dresses = grouped.getOrDefault(ClothesType.DRESS, List.of());
        if (!dresses.isEmpty()) {
            dressBase = buildBaseCombinations(dresses, Optional.empty());
        }

        // 3. 기존 outfitScore 리스트에 나머지 타입 아이템 순차적 추가
        // 3_1) 확장할 타입 순서 고정(아우터 -> 신발 -> ..)
        List<ClothesType> orderedTypes = Arrays.stream(ClothesType.values())
            .filter(t -> !isSkippableType(t))
            .sorted(Comparator.comparingInt(ClothesType::order))
            .toList();

        // 3_1) 기존 코디에서 타입 순서대로 확장
        for (ClothesType type : orderedTypes) {
            List<ClothesScore> items = grouped.getOrDefault(type, List.of());
            if (items.isEmpty()) {
                continue;
            }

            // 상의·하의 조합 확장
            topBottomBase = buildOptionalCombinations(topBottomBase, items, MAX_TOP_BOTTOM_BASE_SIZE, type);

            // 원피스 조합 확장(있으면)
            if (!dressBase.isEmpty()) {
                dressBase = buildOptionalCombinations(dressBase, items, MAX_DRESS_BASE_SIZE, type);
            }
        }

        // 4. 상의/하의와 원피스 리스트 병합
        List<OutfitScore> merged = new ArrayList<>(topBottomBase.size() + dressBase.size());
        merged.addAll(topBottomBase);
        merged.addAll(dressBase);

        logStepResult("최종 결과 리스트", merged);
        return merged;
    }

    /**
     * Outfit 조합 생성 시 건너뛸 의상 타입인지 확인
     */
    private boolean isSkippableType(ClothesType type) {
        return switch (type) {
            case TOP, BOTTOM, DRESS, UNDERWEAR -> true;
            default -> false;
        };
    }

    /**
     * 기본 Outfit 조합 생성
     */
    private List<OutfitScore> buildBaseCombinations(
        List<ClothesScore> base1,
        Optional<List<ClothesScore>> base2
    ) {
        boolean hasSecond = base2.isPresent() && !base2.get().isEmpty();
        String baseType = hasSecond ? "[TOPxBOTTOM]" : "[DRESS]";
        int maxSize = hasSecond ? MAX_TOP_BOTTOM_BASE_SIZE : MAX_DRESS_BASE_SIZE;

        List<OutfitScore> baseCombos = new ArrayList<>();

        if (hasSecond) {
            // 상의 + 하의 조합
            for (ClothesScore top : base1) {
                for (ClothesScore bottom : base2.get()) {
                    baseCombos.add(new OutfitScore().add(top).add(bottom));
                }
            }
        } else {
            // 원피스 단독 추가
            for (ClothesScore dress : base1) {
                baseCombos.add(new OutfitScore().add(dress));
            }
        }

        // 점수순 정렬
        List<OutfitScore> limitedBaseOutfit = buildRankLimited(baseCombos, maxSize);
        logStepResult(baseType + "기본 조합 생성 완료", limitedBaseOutfit);
        return limitedBaseOutfit;
    }

    /**
     * 점수 순 정렬 및 최대 크기만큼만 반환
     */
    private List<OutfitScore> buildRankLimited(List<OutfitScore> outfit, int maxSize) {
        return outfit.stream()
            .sorted(Comparator.comparingDouble(OutfitScore::normalizedScore).reversed())
            .limit(maxSize)
            .toList();
    }

    /**
     * 현재 코디에 특정 타입의 의상 추가
     * 각 기존 Outfit (baseCombo)에 대해 주어진 후보 아이템들을 추가하며,
     * 점수 향상이 일정 수준(IMPROVEMENT_DELTA) 이상인 코디 선택
     * 중복 유사 Outfit은 검출하여 하나로 통합하며, 최종적으로 상위 maxSize개 코디 유지
     */
    private List<OutfitScore> buildOptionalCombinations(
        List<OutfitScore> current,
        List<ClothesScore> candidates,
        int maxSize,
        ClothesType addingType
    ) {
        // 후보 없으면 현재 코디 유지
        if (candidates.isEmpty() || current.isEmpty()) {
            return current;
        }

        List<OutfitScore> generated = new ArrayList<>();

        for (OutfitScore base : current) {
            for (ClothesScore item : candidates) {
                ClothesType type = item.item().type();

                // 어울리지 않는 타입 스킵
                if (isMismatch(base, item)) {
                    continue;
                }

                // 코디 다양성을 위해 세부 아이템 확률적으로 스킵
                if (isProbabilisticOptional(type) && ThreadLocalRandom.current().nextDouble() > 0.6) {
                    continue;
                }

                // 새 조합 점수 계산
                OutfitScore newCombo = new OutfitScore(base).add(item);
                    generated.add(newCombo);
            }

            // 베이스도 후보에 포함
            generated.add(base);
        }

        List<OutfitScore> merged = mergeSimilarOutfits(generated, addingType);
        return limitByScore(merged, maxSize);
    }

    private boolean isProbabilisticOptional(ClothesType type) {
        return switch (type) {
            case ACCESSORY, HAT, SCARF, BAG, SOCKS, UNDERWEAR -> true;
            default -> false;
        };
    }

    /**
     * 기존 코디 리스트에서 새로운 코디 조합과 유사한 기존 조합이 있는지 확인
     * 두 Outfit의 구성 아이템 중 80% 이상 겹치면 같은 코디로 판단
     * 유사도가 0.5 이상인 경우 동일한 조합으로 간주
     * 동일 조합 발견시,
     * 1. 두 조합의 점수 차이가 중복 허용 범위 미만이면 랜덤하게 1개만 유지
     * 2. 새 조합의 점수가 기존보다 높으면 더 높은 쪽을 유지
     */
    private List<OutfitScore> mergeSimilarOutfits(List<OutfitScore> outfits, ClothesType addingType) {
        log.trace("\n[OutfitCombinationGenerator] [{}] 타입 추가 단계 시작 → 총 {}개 후보", addingType, outfits.size());

        // base 기준 그룹화
        Map<String, List<OutfitScore>> groupedByBase = outfits.stream()
            .collect(Collectors.groupingBy(
                outfit -> outfit.getItems().stream()
                    .filter(i -> i.item().type() != addingType)
                    .sorted(Comparator.comparing(i -> i.item().type().name()))
                    .map(i -> i.item().name())
                    .collect(Collectors.joining(", ")),
                LinkedHashMap::new,
                Collectors.toList()
            ));

        List<OutfitScore> merged = new ArrayList<>();
        int baseIndex = 0;

        // 각 그룹 순회
        for (Map.Entry<String, List<OutfitScore>> entry : groupedByBase.entrySet()) {
            baseIndex++;
            String baseKey = entry.getKey();
            List<OutfitScore> candidates = entry.getValue();

            // 후보 점수 정렬 (내림차순)
            candidates.sort(Comparator.comparingDouble(OutfitScore::normalizedScore).reversed());
            double topScore = candidates.get(0).normalizedScore();

            List<OutfitScore> closeCandidates = candidates.stream()
                .filter(c -> (topScore - c.normalizedScore()) <= DUPLICATE_TIE_DELTA)
                .toList();

            // 점수차 미미하면 랜덤 선택
            OutfitScore selected = closeCandidates.get(
                ThreadLocalRandom.current().nextInt(closeCandidates.size())
            );

            // 기본 조합 (추가 아이템 없는 경우 체크)
            OutfitScore baseCandidate = candidates.stream()
                .filter(c -> c.getItems().stream().noneMatch(i -> i.item().type() == addingType))
                .findFirst()
                .orElse(null);

            boolean isBaseSelected = baseCandidate != null && selected == baseCandidate;

            merged.add(selected);

            // 로그 출력
            logExpandResult(baseIndex, addingType, baseKey, candidates, closeCandidates, selected, isBaseSelected);
        }

        log.trace("[OutfitCombinationGenerator]\n[MergeDone] [{}] 타입 병합 완료 → 최종 {}개 코디 남음", addingType, merged.size());

        return merged;
    }

    /** 점수순 상위 maxSize 제한 */
    private List<OutfitScore> limitByScore(List<OutfitScore> list, int maxSize) {
        return list.stream()
            .sorted(Comparator.comparingDouble(OutfitScore::normalizedScore).reversed())
            .limit(maxSize)
            .toList();
    }

    // 스타일 규칙: 코디의 스타일/구성에 기반하여 필터링
    private boolean isMismatch(OutfitScore combo, ClothesScore add) {
        ClothesType addType = add.type();

        if (combo.hasType(ClothesType.DRESS) && addType == ClothesType.HAT) {
            return true;
        }

        // 후드 티 -> 후드 집업 제외
        boolean hasHoodieTop = combo.getItems().stream()
            .anyMatch(i -> i.topType() == TopType.HOODIE);
        boolean isHoodedOuter = addType == ClothesType.OUTER
            && add.outerType() == OuterType.HOODED_JACKET;
        if (hasHoodieTop && isHoodedOuter) {
            return true;
        }

        // 현재 코디가 포멀한 경우: 모자/스카프/악세사리 제외
        if (combo.hasStyle(ClothesStyle.FORMAL)
            && (addType == ClothesType.HAT
            || addType == ClothesType.SCARF
            || addType == ClothesType.ACCESSORY)) {
            return true;
        }

        // 현재 코디가 스포츠/스트릿인 경우: "포멀 아우터" 배제
        boolean comboIsSportyOrStreet =
            combo.hasStyle(ClothesStyle.SPORTY) || combo.hasStyle(ClothesStyle.STREET);

        if (comboIsSportyOrStreet
            && addType == ClothesType.OUTER
            && add.style() == ClothesStyle.FORMAL) {
            return true;
        }

        return false;
    }

    private void logStepResult(String step, List<OutfitScore> combos) {
        String summary = combos.stream()
            .map(c -> String.format("%.1f점 → %s",
                c.normalizedScore(),
                c.getItems().stream()
                    .map(o -> o.item().name() + "(" + o.item().type().name() + ")")
                    .collect(Collectors.joining(", "))))
            .collect(Collectors.joining("\n"));
        log.debug("""
            [OutfitCombinationGenerator] {}
            =====================================
            {}
            =====================================
            """, step, summary);
    }

    /**
     * 각 base 그룹의 후보/결과 로그 출력
     */
    private void logExpandResult(
        int baseIndex,
        ClothesType addingType,
        String baseKey,
        List<OutfitScore> candidates,
        List<OutfitScore> bestCandidates,
        OutfitScore selected,
        boolean isBaseSelected
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n[Expand] base#").append(baseIndex)
            .append(" [").append(addingType).append("]\n")
            .append("├ 기존 코디 구성: ").append(baseKey).append("\n");

        for (OutfitScore c : candidates) {
            sb.append("│  ├ 후보: ").append(c.toString())
                .append(" (점수 ").append(String.format("%.3f", c.normalizedScore())).append(")\n");
        }

        if (bestCandidates.size() > 1) {
            sb.append("│  ├ 점수 차이 미미 ").append(bestCandidates.size()).append("개 중 랜덤 선택됨\n");
        } else {
            sb.append("│  ├ 최고점 후보 선택\n");
        }

        if (isBaseSelected) {
            sb.append("│  └ [결과] 기존 조합 유지: ").append(selected.toString());
        } else {
            sb.append("│  └ [결과] 최종 선택: ").append(selected.toString())
                .append(" (점수 ").append(String.format("%.3f", selected.normalizedScore())).append(")\n");
        }

        log.trace(sb.toString());
    }
}