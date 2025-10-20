package com.sprint.ootd5team.domain.feed.enums;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.ootd5team.domain.feed.dto.enums.SortDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

@DisplayName("SortDirection toSpringDirection 테스트")
class SortDirectionTest {

    @Test
    @DisplayName("ASCENDING은 Sort.Direction.ASC로 매핑된다")
    void ascendingToSpringDirection() {
        assertThat(SortDirection.ASCENDING.toSpringDirection())
            .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("DESCENDING은 Sort.Direction.DESC로 매핑된다")
    void descendingToSpringDirection() {
        assertThat(SortDirection.DESCENDING.toSpringDirection())
            .isEqualTo(Sort.Direction.DESC);
    }
}