package com.sprint.ootd5team.domain.extract.util;

import com.sprint.ootd5team.domain.clothesattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttributeDef;
import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttributeValue;
import com.sprint.ootd5team.domain.clothesattribute.mapper.ClothesAttributeMapper;
import com.sprint.ootd5team.domain.extract.dto.ClothesExtraInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ClothesAttributeUtil {

    private ClothesAttributeUtil() {
    }

    /**
     * LLM에서 추출된 속성 정보를 캐시된 정의값과 매핑하여 DTO 리스트로 변환.
     * <p>
     * 매칭 실패 시:
     * - "기타" 항목이 있으면 "기타"로 대체
     * - "기타" 항목도 없으면 속성은 추가하지 않음
     */
    public static List<ClothesAttributeWithDefDto> buildAttributes(
        ClothesExtraInfo extra,
        Map<String, ClothesAttribute> attribute,
        ClothesAttributeMapper mapper
    ) {
        List<ClothesAttributeWithDefDto> list = new ArrayList<>();

        if (extra == null || extra.attributes() == null) {
            return list;
        }
        if (attribute == null || mapper == null) {
            return list;
        }
        if (!extra.attributes().isEmpty()) {
            extra.attributes().forEach((attrName, rawValue) -> {
                if (rawValue != null && !rawValue.isBlank()) {
                    ClothesAttribute attr = attribute.get(attrName);
                    if (attr != null) {
                        String normalized = normalize(rawValue, attr.getDefs());
                        if (normalized != null && !normalized.isBlank()) {
                            list.add(mapper.toDto(new ClothesAttributeValue(attr, normalized)));
                        }
                    }
                }
            });
        }
        return list;
    }

    /**
     * 원시값을 정의된 selectableValues에 맞게 정규화
     * 예: "라이트 그레이" -> "그레이"
     * 해당 값을 찾을 수 없고 "기타" 항목이 있다면 대체
     * - "기타" 항목도 없으면 ""(빈 문자열) 반환
     */
    public static String normalize(String rawValue, List<ClothesAttributeDef> defs) {
        String trimmed = rawValue.trim();

        // 1. 정확히 일치하는 값 우선 검색
        for (ClothesAttributeDef def : defs) {
            if (trimmed.equals(def.getAttDef())) {
                return def.getAttDef();
            }
        }

        // 2. 부분 매칭 (contains)
        for (ClothesAttributeDef def : defs) {
            if (trimmed.contains(def.getAttDef())) {
                return def.getAttDef();
            }
        }

        // 매칭 실패 → 허용된 값 중 "기타"가 있으면 그걸로 대체
        return defs.stream()
            .map(ClothesAttributeDef::getAttDef)
            .filter(v -> v.equals("기타"))
            .findFirst()
            .orElse("");
    }
}
