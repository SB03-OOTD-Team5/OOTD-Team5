package com.sprint.ootd5team.domain.clothes.repository;

import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @Query(value = """
        SELECT *
        FROM tbl_clothes
        ORDER BY random()
        """, nativeQuery = true)
    List<Clothes> findRandomClothes(Limit limit);

    long countByOwner_Id(UUID ownerId);

    long countByOwner_IdAndType(UUID ownerId, ClothesType type);

    @EntityGraph(attributePaths = {
        "clothesAttributeValues",
        "clothesAttributeValues.attribute",
        "clothesAttributeValues.defValue"
    })
    List<Clothes> findByOwner_Id(@Param("ownerId") UUID ownerId);

    @Query(value = """
    SELECT *
    FROM (
        SELECT c.*,
               ROW_NUMBER() OVER (PARTITION BY c.type ORDER BY RANDOM()) AS rn
        FROM tbl_clothes c
        JOIN tbl_clothes_attributes_values v ON v.clothes_id = c.id
        JOIN tbl_clothes_attributes a ON a.id = v.attribute_id
        WHERE c.owner_id = :ownerId
          AND a.name = '계절'
          AND (
            EXISTS (
              SELECT 1
              FROM unnest(regexp_split_to_array(lower(v.def_value), '[/,\\s]+')) AS tok
              WHERE tok = ANY(:tokens)
            )
            OR (:includeAllSeason = TRUE AND lower(v.def_value) IN ('사계절','기타'))
          )
    ) sub
    WHERE sub.rn <= 10
    """, nativeQuery = true)
    List<UUID> findClothesIdsBySeasonFilter(UUID ownerId, String[] tokens, boolean includeAllSeason);

    @Query("""
    select distinct c
    from Clothes c
    join fetch c.clothesAttributeValues v
    join fetch v.attribute a
    where c.id in :ids
""")
    List<Clothes> findAllWithAttributesByIds(@Param("ids") List<UUID> ids);

}
