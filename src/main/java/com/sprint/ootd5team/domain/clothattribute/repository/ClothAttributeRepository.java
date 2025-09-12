package com.sprint.ootd5team.domain.clothattribute.repository;

import com.sprint.ootd5team.domain.clothattribute.entity.ClothAttribute;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothAttributeRepository extends JpaRepository<ClothAttribute, UUID> {

}
