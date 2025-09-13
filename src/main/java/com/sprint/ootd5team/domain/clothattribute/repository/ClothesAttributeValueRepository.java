package com.sprint.ootd5team.domain.clothattribute.repository;

import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeValue;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClothesAttributeValueRepository extends JpaRepository<ClothesAttributeValue, UUID> {

	// 옷 ID로 모든 속성-값을 가져오되, 속성과 선택지까지 한 번에 패치 (N+1 방지)
	@Query("""
        select distinct cav from ClothesAttributeValue cav
        join fetch cav.attribute a
        left join fetch a.defs d
        where cav.clothes.id = :clothesId
        """)
	List<ClothesAttributeValue> findAllByClothesId(@Param("clothesId") UUID clothesId);

	// 옷 + 속성 조합으로 단일 값 조회 (덮어쓰기 정책용)
	@Query("select cav from ClothesAttributeValue cav where cav.clothes.id = :clothesId and cav.attribute.id = :attributeId")
	Optional<ClothesAttributeValue> findByClothesIdAndAttributeId(@Param("clothesId") UUID clothesId, @Param("attributeId")UUID attributeId);
}
