package com.sprint.ootd5team.domain.clothattribute.repository;

import com.sprint.ootd5team.domain.clothattribute.entity.ClothAttributeValue;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClothesAttributeValueRepository extends JpaRepository<ClothAttributeValue, UUID> {

	// 옷 ID로 모든 속성-값을 가져오되, 속성과 선택지까지 한 번에 패치 (N+1 방지)
	@Query("""
        select cav from ClothAttributeValue cav
        join fetch cav.attribute a
        left join fetch a.defs d
        where cav.clothes.id = :clothesId
        """)
	List<ClothAttributeValue> findAllByClothesId(UUID clothesId);

	// 옷 + 속성 조합으로 단일 값 조회 (덮어쓰기 정책용)
	@Query("select cav from ClothAttributeValue cav where cav.clothes.id = :clothesId and cav.attribute.id = :attributeId")
	Optional<ClothAttributeValue> findByClothesIdAndAttributeId(UUID clothesId, UUID attributeId);
}
