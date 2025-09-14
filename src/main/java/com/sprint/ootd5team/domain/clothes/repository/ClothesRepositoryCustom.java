package com.sprint.ootd5team.domain.clothes.repository;

import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import java.util.List;
import java.util.UUID;

public interface ClothesRepositoryCustom {

    List<Clothes> findClothes(
        UUID ownerId,
        ClothesType type,
        String cursor,
        UUID idAfter,
        int limit
    );
}
