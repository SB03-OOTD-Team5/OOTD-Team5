package com.sprint.ootd5team.domain.recommendation.enums.util;

import com.sprint.ootd5team.domain.recommendation.enums.type.BottomType;
import com.sprint.ootd5team.domain.recommendation.enums.type.OuterType;
import com.sprint.ootd5team.domain.recommendation.enums.type.ShoesType;
import com.sprint.ootd5team.domain.recommendation.enums.type.TopType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 아이템 간 패널티(감점) 정의 클래스
 * - 어울리지 않는 조합에 대해 음수 점수를 부여
 * - 명시되지 않은 조합은 0점 (중립)
 */
public final class OutfitPenalty {

    /** (Enum1, Enum2) → penalty 점수 */
    private static final Map<Key, Double> PENALTY_MAP = new HashMap<>();

    static {
        // 상의 + 하의
        addPenalty(TopType.BLOUSE, BottomType.JOGGER, -3);

        // 아우터 + 상의
        addPenalty(OuterType.COAT, TopType.HOODIE, -3);
        addPenalty(OuterType.FUR_MUSTANG, TopType.HOODIE, -3);
        addPenalty(OuterType.HOOD_ZIPUP, TopType.BLOUSE, -3);
        addPenalty(OuterType.BLAZER, TopType.HOODIE, -3);

        // 아우터 + 하의
        addPenalty(OuterType.COAT, BottomType.JOGGER, -3);
        addPenalty(OuterType.TRENCH_COAT, BottomType.JOGGER, -3);
        addPenalty(OuterType.BLAZER, BottomType.JOGGER, -3);
        addPenalty(OuterType.PADDING, BottomType.SHORTS, -3);

        // 하의 + 신발
        addPenalty(BottomType.SLACKS, ShoesType.RAIN_BOOTS, -3);
        addPenalty(BottomType.JOGGER, ShoesType.LOAFERS, -3);
        addPenalty(BottomType.JOGGER, ShoesType.HEELS, -3);

        // 아우터 + 신발
        addPenalty(OuterType.COAT, ShoesType.LOAFERS, 1);
        addPenalty(OuterType.COAT, ShoesType.HEELS, 1);
    }

    private OutfitPenalty() {}

    /** 내부용 Key (Enum 쌍) */
    private record Key(Enum<?> a, Enum<?> b) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key key)) return false;
            // 순서 무시: (A,B) == (B,A)
            return (a.equals(key.a) && b.equals(key.b))
                || (a.equals(key.b) && b.equals(key.a));
        }

        @Override
        public int hashCode() {
            // 순서 무시를 위한 hash
            return Objects.hash(a.name().compareTo(b.name()) < 0 ? a : b,
                a.name().compareTo(b.name()) < 0 ? b : a);
        }
    }

    private static void addPenalty(Enum<?> a, Enum<?> b, double score) {
        PENALTY_MAP.put(new Key(a, b), score);
    }

    public static double getPenalty(Enum<?> a, Enum<?> b) {
        if (a == null || b == null) return 0.0;
        return PENALTY_MAP.getOrDefault(new Key(a, b), 0.0);
    }
}