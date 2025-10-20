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

    @Query("""
       SELECT DISTINCT c
       FROM Clothes c
       LEFT JOIN FETCH c.clothesAttributeValues cav
       LEFT JOIN FETCH cav.attribute attr
       WHERE c.owner.id = :ownerId
         AND EXISTS (
            SELECT 1
            FROM ClothesAttributeValue cav2
            JOIN cav2.attribute attr2
            WHERE cav2.clothes = c
              AND attr2.name = '계절'
         )
    """)
    List<Clothes> findByOwnerWithSeasonAttribute(@Param("ownerId") UUID ownerId);


}
