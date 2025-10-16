package com.sprint.ootd5team.domain.recommendation.engine.model;

import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.recommendation.enums.ClothesStyle;
import com.sprint.ootd5team.domain.recommendation.enums.ColorTone;
import com.sprint.ootd5team.domain.recommendation.enums.Material;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 코디 조합의 점수와 누적 속성(톤/소재/스타일)을 관리
 * <p>
 * - add(...) 호출로 아이템을 추가할 때, 기존 아이템들과의 페어와이즈 조화 점수(톤/소재/스타일)를
 * 계산하여 totalScore에 누적
 * - normalizedScore()는 누적 조화 점수에 조합 수준 보정
 */
@Slf4j
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OutfitScore {

    private List<ClothesScore> items = new ArrayList<>();
    private double totalScore = 0.0;

    private List<ColorTone> tones = new ArrayList<>();
    private List<Material> materials = new ArrayList<>();
    private List<ClothesStyle> styles = new ArrayList<>();

    public OutfitScore(OutfitScore other) {
        this.items = new ArrayList<>(other.items);
        this.totalScore = other.totalScore;
        this.tones = new ArrayList<>(other.tones);
        this.materials = new ArrayList<>(other.materials);
        this.styles = new ArrayList<>(other.styles);
    }

    /**
     * 아이템을 추가하고, 기존 조합과의 조화에 따른 증분 점수를 누적
     */
    public OutfitScore add(ClothesScore clothes) {
        items.add(clothes);
        tones.add(clothes.tone());
        materials.add(clothes.material());
        styles.add(clothes.style());

        // 아이템 추가 시 기존 조합과 비교
        double delta = calculateIncrementalScore(clothes);
        totalScore += delta;
        return this;
    }

    /**
     * 새 아이템 추가 시 기존 조합과 비교하여 가산점/감점 계산
     */
    private double calculateIncrementalScore(ClothesScore added) {
        double delta = 0.0;

        List<ColorTone> prevTones = tones.subList(0, tones.size() - 1);
        List<Material> prevMats = materials.subList(0, materials.size() - 1);
        List<ClothesStyle> prevStyles = styles.subList(0, styles.size() - 1);

        for (ColorTone existingTone : prevTones) {
            delta += added.tone().getHarmonyScore(existingTone);
        }

        for (Material existingMat : prevMats) {
            delta += added.material().getCompatibilityScore(existingMat);
        }

        for (ClothesStyle existingStyle : prevStyles) {
            delta += added.style().getHarmonyScore(existingStyle);
        }

        if (added.item().type() == ClothesType.SHOES && added.item().shoesType() != null) {
            Map<ClothesStyle, Long> freq = prevStyles.stream()
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

            long total = freq.values().stream().mapToLong(Long::longValue).sum();
            double weightedScore = freq.entrySet().stream()
                .mapToDouble(e -> {
                    double ratio = (double) e.getValue() / total;
                    return added.item().shoesType().getClothesStyle(e.getKey()) * ratio;
                })
                .sum();

            delta += weightedScore;
        }

        if (prevTones.stream().distinct().count() == 1 && prevTones.size() > 1) {
            delta += 1;
        }

        return delta;
    }

    /**
     * 누적 점수와 조합 수준 보정을 반영한 최종 점수 반환
     * <p>
     * - 세트 보너스: 단품보다 조합
     * - 누적 조화 정규화: 아이템 수가 늘어날수록 과도하게 유리해지는 것을 보정
     * - 드레스/상의·하의 기반 보정: 조합 구조별 완성도 보너스
     * - 전역 스타일 일관성: 전체가 하나의 스타일로 정렬될수록 보너스
     */
    public double normalizedScore() {
        double comboBonus = Math.log(items.size() + 1) * 1.5;
        double normalizedTotal = totalScore / Math.pow(items.size(), 1.2);
        double score = normalizedTotal + comboBonus;

        boolean hasDress = contains(ClothesType.DRESS);
        boolean hasTop = contains(ClothesType.TOP);
        boolean hasBottom = contains(ClothesType.BOTTOM);
        boolean hasOuter = contains(ClothesType.OUTER);
        boolean hasShoes = contains(ClothesType.SHOES);
        boolean hasAccessory = items.stream().anyMatch(i ->
            switch (i.item().type()) {
                case ACCESSORY, BAG, HAT, SCARF -> true;
                default -> false;
            });

        // 드레스 기반
        if (hasDress) {
            score += 1.0;
            if (hasOuter) score += 0.5;
            if (hasShoes) score += 0.5;
        }

        // 상/하의 기반
        if (hasTop && hasBottom) {
            if (hasOuter) score += 0.3;
            if (hasShoes) score += 0.3;
            if (hasAccessory) score += 0.2;
        }

        // 세트 개수 보너스
        if (items.size() >= 5) {
            score += 0.2 * Math.min(items.size(), 7);
        }

        // 타일 일관성
        if (styles.size() > 1 && styles.stream().distinct().count() == 1) {
            score += 0.8;
        }

        return score;
    }

    private boolean contains(ClothesType type) {
        return items.stream().anyMatch(i -> i.item().type() == type);
    }

    public double score() {
        return totalScore;
    }

    @Override
    public String toString() {
        return items.stream()
            .map(i -> i.item().name() + "(" + i.item().type().name() + ")")
            .collect(Collectors.joining(", "));
    }
}