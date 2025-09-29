package com.sprint.ootd5team.domain.clothesattribute.repository;

import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttributeValue;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClothesAttributeValueRepository extends JpaRepository<ClothesAttributeValue, UUID> {

	@Query("""
        select distinct cav from ClothesAttributeValue cav
        join fetch cav.attribute a
        left join fetch a.defs d
        where cav.clothes.id = :clothesId
        """)
	List<ClothesAttributeValue> findAllByClothesId(@Param("clothesId") UUID clothesId);

	@Query("select cav from ClothesAttributeValue cav where cav.clothes.id = :clothesId and cav.attribute.id = :attributeId")
	Optional<ClothesAttributeValue> findByClothesIdAndAttributeId(@Param("clothesId") UUID clothesId, @Param("attributeId")UUID attributeId);

	boolean existsByAttribute_Id(UUID attributeId);
}
