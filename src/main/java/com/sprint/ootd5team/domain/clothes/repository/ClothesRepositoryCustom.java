package com.sprint.ootd5team.domain.clothes.repository;

import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;

public interface ClothesRepositoryCustom {

    List<Clothes> findByUserWithCursor(
        UUID ownerId,
        ClothesType type,
        Instant cursor,
        UUID idAfter,
        int limit,
        Sort.Direction sortDirection
    );
}
