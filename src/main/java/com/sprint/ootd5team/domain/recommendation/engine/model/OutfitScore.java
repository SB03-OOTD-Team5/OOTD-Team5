package com.sprint.ootd5team.domain.recommendation.engine.model;

import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.recommendation.enums.ClothesStyle;
import com.sprint.ootd5team.domain.recommendation.enums.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 코디 조합의 점수와 누적 속성(톤/소재/스타일)을 관리
 * <p>
 * - add(...) 호출로 아이템을 추가할 때, 기존 아이템들과의 조화 점수(톤/소재/스타일)를
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

    public OutfitScore(OutfitScore other) {
        this.items = new ArrayList<>(other.items);
        this.totalScore = other.totalScore;
    }

    public OutfitScore add(ClothesScore clothes) {
        items.add(clothes);
        totalScore += calculateIncrementalScore(clothes);
        return this;
    }

    public boolean hasType(ClothesType type) {
        return items.stream().anyMatch(i -> i.type() == type);
    }

    public boolean hasStyle(ClothesStyle style) {
        return items.stream().anyMatch(i -> i.style() == style);
    }

    /**
     * 새 아이템 추가 시 기존 조합과 비교하여 가산점/감점 계산
     */
    private double calculateIncrementalScore(ClothesScore added) {
        double delta = 0.0;
        if (items.size() <= 1) return delta;

        // ETC 타입은 조화 점수 계산 제외
        if (added.item().type() == ClothesType.ETC || added.item().type() == ClothesType.SOCKS) {
            return 0.0;
        }

        // 기존 조합
        List<ClothesScore> prevItems = items.subList(0, items.size() - 1).stream()
            .filter(p -> p.item().type() != ClothesType.ETC && p.item().type() != ClothesType.SOCKS)
            .toList();

        for (ClothesScore prev : prevItems) {
            // 색상 조화
            if (added.color() != null && prev.color() != null) {
                delta += added.color().getColorMatchBonus(prev.color());
            }

            // 톤 조화
            if (added.tone() != null && prev.tone() != null) {
                delta += added.tone().getHarmonyScore(prev.tone());
            }

            // 소재 조화
            if (added.material() != null && prev.material() != null) {
                delta += added.material().getCompatibilityScore(prev.material());
            }

            // 스타일 조화
            if (added.style() != null && prev.style() != null) {
                delta += added.style().getHarmonyScore(prev.style());
            }

            // 신발 타입 보정
            if (added.type() == ClothesType.SHOES && added.shoesType() != null) {
                delta += added.shoesType().getClothesStyleFromShoes(prev.style());
            }

            // 상하의 색상 보정
            if (added.type() == ClothesType.OUTER && added.color() != null) {
                Color topColor = getColorOf(ClothesType.TOP);
                Color bottomColor = getColorOf(ClothesType.BOTTOM);
                Color dressColor = getColorOf(ClothesType.DRESS);

                double colorPenalty = added.color().getOuterContrastPenalty(topColor, bottomColor, dressColor);
                if (colorPenalty != 0) {
                    delta += colorPenalty;
                }
            }

            if (added.type() == ClothesType.ACCESSORY
                || added.type() == ClothesType.BAG
                || added.type() == ClothesType.HAT
                || added.type() == ClothesType.SCARF) {
                delta *= 0.5;
            }

        }

        return delta;
    }

    /**
     * 최종 정규화 점수 계산
     */
    public double normalizedScore() {
        List<ClothesScore> validItems = new ArrayList<>(items);

        int n = Math.max(1, validItems.size());
        double total = validItems.stream()
            .mapToDouble(ClothesScore::score)
            .sum();

        // 평균 점수 (모든 의상 포함)
        double avgScore = (total + totalScore) / n;
        double score = avgScore;

        // 스타일 일관성
        List<ClothesStyle> styles = validItems.stream()
            .map(i -> i.item().style())
            .filter(Objects::nonNull)
            .toList();

        if (styles.size() > 1 && styles.stream().distinct().count() == 1) {
            score += 0.5;
        }

        // ETC 가중치(우산, 손수건 등..)
        boolean hasEtc = validItems.stream()
            .anyMatch(i -> i.item().type() == ClothesType.ETC || i.item().type() == ClothesType.SOCKS);

        if (hasEtc) {
            score *= 1.1;
        }

        return score;
    }

    public double score() {
        return totalScore;
    }

    private Color getColorOf(ClothesType type) {
        return items.stream()
            .filter(i -> i.type() == type)
            .map(ClothesScore::color)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    @Override
    public String toString() {
        return items.stream()
            .map(i -> i.name() + "(" + i.type().name() + ")")
            .collect(Collectors.joining(", "));
    }
}