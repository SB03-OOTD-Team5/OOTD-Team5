package com.sprint.ootd5team.domain.like.entity;

import com.sprint.ootd5team.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "tbl_feed_likes")
@Entity
public class FeedLike extends BaseEntity {

    @Column(name = "feed_id", nullable = false)
    private UUID feedId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

}