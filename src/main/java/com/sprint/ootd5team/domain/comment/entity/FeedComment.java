package com.sprint.ootd5team.domain.comment.entity;

import com.sprint.ootd5team.base.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(
    name = "tbl_feed_comments",
    indexes = {
        @Index(name = "idx_feed_comments_feed_id", columnList = "feed_id")
    }
)
@Entity
public class FeedComment extends BaseUpdatableEntity {

    @Column(name = "feed_id", nullable = false)
    private UUID feedId;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "content", nullable = false)
    private String content;
}