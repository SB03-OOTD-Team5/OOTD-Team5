package com.sprint.ootd5team.domain.clothes.repository;

import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClothesRepository extends JpaRepository<Clothes, UUID>, ClothesRepositoryCustom {

    @Query(value = """
            SELECT DISTINCT c.*
            FROM tbl_feeds AS f
            JOIN tbl_feed_clothes AS fc ON fc.feed_id = f.id
            JOIN tbl_clothes AS c ON fc.clothes_id = c.id
            WHERE f.weather_id IN (:weatherIds)
        """, nativeQuery = true)
    List<Clothes> findClothesInWeatherIds(
        @Param("weatherIds") Collection<UUID> weatherIds);

    List<Clothes> findByIdNotIn(Collection<UUID> ids, Limit limit);

}
