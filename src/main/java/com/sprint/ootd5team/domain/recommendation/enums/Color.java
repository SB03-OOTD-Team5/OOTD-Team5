package com.sprint.ootd5team.domain.recommendation.enums;

import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum Color {
    NAVY("네이비", new String[]{"navy"}, ColorTone.COOL),
    SKY_BLUE("스카이블루", new String[]{"skyblue", "하늘색"}, ColorTone.COOL),
    ORANGE("오렌지", new String[]{"orange"}, ColorTone.WARM),
    MINT("민트", new String[]{"mint"}, ColorTone.COOL),
    LAVENDER("라벤더", new String[]{"lavender"}, ColorTone.COOL),
    KHAKI("카키", new String[]{"khaki"}, ColorTone.WARM),
    WINE("와인", new String[]{"wine", "버건디"}, ColorTone.WARM),
    SILVER("실버", new String[]{"silver", "은색"}, ColorTone.NEUTRAL),
    GOLD("골드", new String[]{"gold", "금색"}, ColorTone.WARM),
    BROWN("브라운", new String[]{"brown", "갈색"}, ColorTone.WARM),
    GRAY("그레이", new String[]{"gray", "회색"}, ColorTone.NEUTRAL),
    WHITE("화이트", new String[]{"white", "흰색"}, ColorTone.NEUTRAL),
    BLACK("블랙", new String[]{"black", "검정"}, ColorTone.NEUTRAL),
    BLUE("블루", new String[]{"blue", "파랑"}, ColorTone.COOL),
    GREEN("그린", new String[]{"green", "초록"}, ColorTone.COOL),
    PINK("핑크", new String[]{"pink", "분홍"}, ColorTone.WARM),
    RED("레드", new String[]{"red", "빨강"}, ColorTone.WARM),
    BEIGE("베이지", new String[]{"beige"}, ColorTone.WARM),
    PURPLE("퍼플", new String[]{"purple", "보라"}, ColorTone.COOL),
    YELLOW("옐로우", new String[]{"yellow", "노랑"}, ColorTone.WARM),
    OTHER("기타", new String[]{}, ColorTone.OTHER);

    private final String displayName;
    private final String[] aliases;
    private final ColorTone tone;

    Color(String displayName, String[] aliases, ColorTone tone) {
        this.displayName = displayName;
        this.aliases = aliases;
        this.tone = tone;
    }

    public ColorTone tone() {
        return tone;
    }

    public String displayName() {
        return displayName;
    }

    /** 밝은 색상인지 여부 */
    public boolean isBright() {
        return switch (this) {
            case WHITE, BEIGE, SKY_BLUE, SILVER, PINK, YELLOW -> true;
            default -> false;
        };
    }

    /** 어두운 색상인지 여부 */
    public boolean isDark() {
        return switch (this) {
            case BLACK, NAVY, GRAY, BROWN, WINE, KHAKI -> true;
            default -> false;
        };
    }

    public double getColorMatchBonus(Color other) {
        if (this == other) return 1.0; // 같은 색은 기본 조화

        // 상하의 조합 추천 매핑
        return switch (this) {
            case NAVY -> switch (other) {
                case BEIGE, SKY_BLUE -> 2.0;
                case BLUE -> 1.0;
                case BLACK -> -1.0;
                default -> 0.0;
            };
            case GREEN -> switch (other) {
                case BLACK, SKY_BLUE -> 2.0;
                case KHAKI -> -2.0;
                default -> 0.0;
            };
            case BEIGE -> switch (other) {
                case WHITE, NAVY, BROWN -> 1.5;
                case BLACK -> 1.0;
                default -> 0.0;
            };
            case WHITE -> switch (other) {
                case NAVY, BEIGE, KHAKI, GRAY -> 1.5;
                default -> 0.0;
            };
            case BLUE -> switch (other) {
                case BLACK, WHITE, BEIGE -> 1.5;
                case NAVY -> 1.0;
                default -> 0.0;
            };
            case BLACK -> switch (other) {
                case BEIGE, WHITE, GRAY, KHAKI -> 1.5;
                case NAVY -> 0.5;
                default -> 0.0;
            };
            case BROWN -> switch (other) {
                case BEIGE, WHITE, KHAKI -> 1.0;
                default -> 0.0;
            };
            case GRAY -> switch (other) {
                case WHITE, PINK, NAVY, PURPLE -> 1.0;
                default -> 0.0;
            };
            default -> 0.0;
        };
    }

    /**
     * OUTER(겉옷)가 TOP/BOTTOM과 색상적으로 너무 유사하면 감점
     */
    public double getOuterContrastPenalty(Color top, Color bottom, Color dress) {
        double penalty = 0.0;

        // DRESS 우선 적용 (상의·하의 모두 대체)
        if (dress != null) {
            if (this == dress) {
                penalty -= 1.0;
            }

            return penalty;
        }

        // 일반 코디 (TOP + BOTTOM 비교)
        if (this == OTHER) return 0.0;

        if (top != null && this == top) {
            penalty -= 1.0;
        } else if (top != null && this.tone == top.tone) {
            penalty -= 0.5;
        }

        if (bottom != null && this == bottom) {
            penalty -= 0.8;
        }

        return penalty;
    }
}
