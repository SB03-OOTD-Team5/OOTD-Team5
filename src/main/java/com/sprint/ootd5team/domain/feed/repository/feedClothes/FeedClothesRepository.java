package com.sprint.ootd5team.domain.feed.repository.feedClothes;

import com.sprint.ootd5team.domain.feed.entity.FeedClothes;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedClothesRepository extends JpaRepository<FeedClothes, UUID>, FeedClothesRepositoryCustom {

}