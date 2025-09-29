package com.sprint.ootd5team.domain.follow.entity;

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
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(
    name = "tbl_follows",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_follows", columnNames = {"followee_id", "follower_id"})
    },
    indexes = {
        @Index(name = "idx_follows_followee_id", columnList = "followee_id"),
        @Index(name = "idx_follows_follower_id", columnList = "follower_id")
    }
)
public class Follow extends BaseEntity {

    @Column(name = "followee_id", nullable = false)
    private UUID followeeId;

    @Column(name = "follower_id", nullable = false)
    private UUID followerId;
}