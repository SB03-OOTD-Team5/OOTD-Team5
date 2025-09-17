package com.sprint.ootd5team.domain.like.entity;

import com.sprint.ootd5team.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(
    name = "tbl_feed_likes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_feed_like", columnNames = {"feed_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_feed_likes_feed_id", columnList = "feed_id"),
        @Index(name = "idx_feed_likes_user_id", columnList = "user_id")
    }
)
@Entity
public class FeedLike extends BaseEntity {

    @Column(name = "feed_id", nullable = false)
    private UUID feedId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

}