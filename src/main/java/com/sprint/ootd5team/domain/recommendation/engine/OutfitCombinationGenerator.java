package com.sprint.ootd5team.domain.recommendation.engine;

import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.recommendation.engine.model.ClothesScore;
import com.sprint.ootd5team.domain.recommendation.engine.model.OutfitScore;
import com.sprint.ootd5team.domain.recommendation.enums.ClothesStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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

    private static final double IMPROVEMENT_DELTA = 0.2;     // 선택 아이템 추가 시 최소 개선폭
    private static final double DUPLICATE_TIE_DELTA = 0.2;   // 중복 충돌 시 점수 동률 허용 범위

    public List<OutfitScore> generateWithScoring(List<ClothesScore> candidates) {
        Map<ClothesType, List<ClothesScore>> grouped = candidates.stream()
            .collect(Collectors.groupingBy(c -> c.item().type()));

        List<ClothesScore> tops = grouped.getOrDefault(ClothesType.TOP, List.of());
        List<ClothesScore> bottoms = grouped.getOrDefault(ClothesType.BOTTOM, List.of());
        List<ClothesScore> dresses = grouped.getOrDefault(ClothesType.DRESS, List.of());

        // 단계 로그: 후보 수 요약
        log.debug(
            "[OutfitCombinationGenerator] [조합생성] tops={}, bottoms={}, dresses={}, optionalTypes={}",
            tops.size(), bottoms.size(), dresses.size(),
            grouped.keySet().stream().filter(t -> !isSkippableType(t)).map(Enum::name).toList());

        List<OutfitScore> topBottomBase = buildTopBottomCombinations(tops, bottoms);
        List<OutfitScore> dressBase = buildDressCombinations(dresses);

        // 선택 아이템 확장 (타입별로 독립 확장)
        for (ClothesType type : ClothesType.values()) {
            if (isSkippableType(type)) {
                continue;
            }

            List<ClothesScore> items = grouped.getOrDefault(type, List.of());
            if (items.isEmpty()) {
                continue;
            }

            int perBaseBeam = beamWidthForType(type, items.size());

            topBottomBase = buildOptionalCombinations(topBottomBase, items,
                MAX_TOP_BOTTOM_BASE_SIZE, type, perBaseBeam);
            dressBase = buildOptionalCombinations(dressBase, items, MAX_DRESS_BASE_SIZE, type,
                perBaseBeam);
        }

        // 병합 후 상위만 유지
        List<OutfitScore> merged = new ArrayList<>(topBottomBase.size() + dressBase.size());
        merged.addAll(topBottomBase);
        merged.addAll(dressBase);

        merged = merged.stream()
            .sorted(Comparator.comparingDouble(OutfitScore::normalizedScore).reversed())
            .limit(MAX_TOP_BOTTOM_BASE_SIZE + MAX_DRESS_BASE_SIZE)
            .toList();

        logStepResult("최종 결과 리스트", merged);
        return merged;
    }

    private boolean isSkippableType(ClothesType type) {
        return switch (type) {
            case TOP, BOTTOM, DRESS -> true;
            default -> false;
        };
    }

    private List<OutfitScore> buildTopBottomCombinations(
        List<ClothesScore> tops,
        List<ClothesScore> bottoms
    ) {
        List<OutfitScore> result = new ArrayList<>();
        for (ClothesScore top : tops) {
            for (ClothesScore bottom : bottoms) {
                OutfitScore outfit = new OutfitScore().add(top).add(bottom);
                result.add(outfit);
            }
        }

        List<OutfitScore> limited = result.stream()
            .sorted(Comparator.comparingDouble(OutfitScore::normalizedScore).reversed())
            .limit(MAX_TOP_BOTTOM_BASE_SIZE)
            .toList();

        log.debug("[OutfitCombinationGenerator] 상의&하의 기본 조합: 총 {}건 -> 상위 {}건", result.size(), limited.size());
        return limited;
    }

    private List<OutfitScore> buildDressCombinations(List<ClothesScore> dresses) {
        List<OutfitScore> result = dresses.stream()
            .map(dress -> new OutfitScore().add(dress))
            .sorted(Comparator.comparingDouble(OutfitScore::normalizedScore).reversed())
            .limit(MAX_DRESS_BASE_SIZE)
            .toList();

        log.debug("[OutfitCombinationGenerator] 원피스 기본 조합: 총 {}건 -> 상위 {}건", dresses.size(), result.size());
        return result;
    }

    private int beamWidthForType(ClothesType type, int candidateCount) {
        return switch (type) {
            case OUTER, SHOES -> Math.min(3, candidateCount);
            case ACCESSORY, HAT, BAG, SCARF, SOCKS -> Math.min(1, candidateCount);
            default -> Math.min(2, candidateCount);
        };
    }

    private List<OutfitScore> buildOptionalCombinations(
        List<OutfitScore> current,
        List<ClothesScore> candidates,
        int maxSize,
        ClothesType addingType,
        int perBaseBeamWidth
    ) {
        if (candidates.isEmpty() || current.isEmpty()) {
            return current;
        }
        List<OutfitScore> evolved = new ArrayList<>(
            Math.min(maxSize, current.size() * perBaseBeamWidth));
        int mismatchCount = 0;
        int duplicateHitCount = 0;
        int improvedBaseCount = 0;

        for (OutfitScore baseCombo : current) {
            double baseScore = baseCombo.normalizedScore();

            List<OutfitScore> improved = new ArrayList<>(perBaseBeamWidth);
            for (ClothesScore item : candidates) {
                if (isMismatch(baseCombo, item)) {
                    mismatchCount++;
                    continue;
                }
                OutfitScore newCombo = new OutfitScore(baseCombo).add(item);
                double newScore = newCombo.normalizedScore();
                if (newScore > baseScore + IMPROVEMENT_DELTA) {
                    improved.add(newCombo);
                }
            }

            // 상위 k개만 선택
            if (!improved.isEmpty()) {
                improvedBaseCount++;
                List<OutfitScore> sortedImproved = improved.stream()
                    .sorted(Comparator.comparingDouble(OutfitScore::normalizedScore).reversed())
                    .limit(perBaseBeamWidth)
                    .toList();

                for (OutfitScore c : sortedImproved) {
                    OutfitScore dup = findSimilar(evolved, c);
                    if (dup == null) {
                        evolved.add(c);
                    } else {
                        duplicateHitCount++;
                    }
                }
            } else {
                evolved.add(baseCombo);
            }
        }

        List<OutfitScore> limited = evolved.stream()
            .sorted(Comparator.comparingDouble(OutfitScore::normalizedScore).reversed())
            .limit(maxSize)
            .toList();

        log.debug(
            "[선택확장/{}] 입력:{} -> 출력:{} (mismatch:{}, dupHit:{}, improvedBase:{}, perBaseBeam:{})",
            addingType, current.size(), limited.size(), mismatchCount, duplicateHitCount,
            improvedBaseCount, perBaseBeamWidth);

        return limited;
    }

    private OutfitScore findSimilar(List<OutfitScore> list, OutfitScore candidate) {
        for (int i = 0; i < list.size(); i++) {
            OutfitScore existing = list.get(i);
            double overlap = calculateOverlap(existing, candidate);
            if (overlap >= 0.5) {
                double existingScore = existing.normalizedScore();
                double candidateScore = candidate.normalizedScore();
                double scoreDiff = Math.abs(existingScore - candidateScore);

                if (scoreDiff < DUPLICATE_TIE_DELTA) {
                    boolean replace = ThreadLocalRandom.current().nextBoolean();
                    if (replace) {
                        list.set(i, candidate);
                        log.debug("[OutfitCombinationGenerator] [중복랜덤] 점수차 {} -> 교체됨: {}",
                            String.format("%.2f", scoreDiff),
                            candidate);
                    } else {
                        log.debug("[OutfitCombinationGenerator] [중복랜덤] 점수차 {} -> 기존 유지: {}",
                            String.format("%.2f", scoreDiff),
                            existing);
                    }
                } else if (candidateScore > existingScore) {
                    list.set(i, candidate);
                    log.debug("[OutfitCombinationGenerator] [중복교체] 기존보다 {} 높음 -> 교체됨: {}",
                        String.format("%.2f", candidateScore - existingScore), existing);
                }
                return existing;
            }
        }
        return null;
    }

    private double calculateOverlap(OutfitScore a, OutfitScore b) {
        Set<UUID> idsA = a.getItems().stream()
            .map(i -> i.item().clothesId())
            .collect(Collectors.toSet());
        Set<UUID> idsB = b.getItems().stream()
            .map(i -> i.item().clothesId())
            .collect(Collectors.toSet());

        int intersection = (int) idsA.stream().filter(idsB::contains).count();
        int union = idsA.size() + idsB.size() - intersection;
        return union == 0 ? 0 : (double) intersection / union;
    }

    // 스타일 규칙: 코디의 스타일/구성에 기반하여 필터링
    private boolean isMismatch(OutfitScore combo, ClothesScore add) {
        ClothesType addType = add.item().type();

        boolean hasDress = combo.getItems().stream()
            .anyMatch(i -> i.item().type() == ClothesType.DRESS);

        // 현재 코디가 포멀한 경우: 모자/스카프/악세사리 제외
        boolean comboIsFormal = combo.getStyles().contains(ClothesStyle.FORMAL);
        if (comboIsFormal && (addType == ClothesType.HAT || addType == ClothesType.SCARF
            || addType == ClothesType.ACCESSORY)) {
            return true;
        }

        // 현재 코디가 스포츠/스트릿인 경우: "포멀 아우터" 배제
        boolean comboIsSportyOrStreet = combo.getStyles().contains(ClothesStyle.SPORTY)
            || combo.getStyles().contains(ClothesStyle.STREET);
        if (comboIsSportyOrStreet
            && addType == ClothesType.OUTER
            && add.style() == ClothesStyle.FORMAL) {
            return true;
        }

        // 원피스 코디에는 모자 배제
        if (hasDress && addType == ClothesType.HAT) {
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
        log.info("""
            [OutfitCombinationGenerator] {}
            =====================================
            {}
            =====================================
            """, step, summary);
    }
}