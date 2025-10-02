package com.sprint.ootd5team.domain.clothesattribute.repository;

import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttribute;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClothesAttributeRepository extends JpaRepository<ClothesAttribute, UUID> {
	boolean existsByNameIgnoreCase(String name);

	@EntityGraph(attributePaths = "defs")
	@Query("select ca from ClothesAttribute ca")
	List<ClothesAttribute> findAllWithDefs();
}
