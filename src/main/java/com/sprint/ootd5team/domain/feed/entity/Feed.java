package com.sprint.ootd5team.domain.feed.entity;

import com.sprint.ootd5team.base.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "tbl_feeds")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Feed extends BaseUpdatableEntity {

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "weather_id", nullable = false)
    private UUID weatherId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "comment_count", nullable = false)
    private long commentCount = 0;

    @Column(name = "like_count", nullable = false)
    private long likeCount = 0;

    public void updateContent(String content) {
        this.content = content;
    }
}