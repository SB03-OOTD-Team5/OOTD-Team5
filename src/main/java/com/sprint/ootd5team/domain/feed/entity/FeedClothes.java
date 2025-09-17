package com.sprint.ootd5team.domain.feed.entity;

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

@Table(
    name = "tbl_feed_clothes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_feed_clothes", columnNames = {"feed_id", "clothes_id"})
    },
    indexes = {
        @Index(name = "idx_feed_clothes_feed_id", columnList = "feed_id"),
        @Index(name = "idx_feed_clothes_clothes_id", columnList = "clothes_id")
    }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class FeedClothes extends BaseEntity {

    @Column(name = "feed_id", nullable = false)
    private UUID feedId;

    @Column(name = "clothes_id", nullable = false)
    private UUID clothesId;
}