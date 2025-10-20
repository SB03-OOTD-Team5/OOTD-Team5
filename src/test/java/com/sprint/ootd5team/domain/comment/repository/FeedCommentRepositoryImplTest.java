package com.sprint.ootd5team.domain.comment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.ootd5team.base.config.JpaAuditingConfig;
import com.sprint.ootd5team.base.config.QuerydslConfig;
import com.sprint.ootd5team.domain.comment.dto.data.CommentDto;
import com.sprint.ootd5team.domain.comment.entity.FeedComment;
import com.sprint.ootd5team.domain.comment.repository.impl.FeedCommentRepositoryImpl;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({QuerydslConfig.class, JpaAuditingConfig.class})
@ActiveProfiles("test")
@DisplayName("FeedCommentRepositoryImpl 테스트")
public class FeedCommentRepositoryImplTest {

    @Autowired
    private FeedCommentRepositoryImpl feedCommentRepositoryImpl;

    @Autowired
    private TestEntityManager em;

    private UUID feedId;

    @BeforeEach
    void setUp() throws InterruptedException {
        feedId = UUID.randomUUID();

        FeedComment c1 = new FeedComment(feedId, UUID.randomUUID(), "댓글1");
        em.persist(c1);
        em.flush();

        Thread.sleep(5);

        FeedComment c2 = new FeedComment(feedId, UUID.randomUUID(), "댓글2");
        em.persist(c2);
        em.flush();

        Thread.sleep(5);

        FeedComment c3 = new FeedComment(feedId, UUID.randomUUID(), "댓글3");
        em.persist(c3);
        em.flush();

        em.clear();
    }

    @Test
    @DisplayName("기본 조회 - 최신순 정렬, limit 동작 확인")
    void findByFeedIdWithCursor_basic() {
        List<CommentDto> results = feedCommentRepositoryImpl.findByFeedIdWithCursor(feedId, null, null, 2);

        assertThat(results).hasSize(3);
        assertThat(results.get(0).content()).isEqualTo("댓글3");
        assertThat(results.get(1).content()).isEqualTo("댓글2");
        assertThat(results.get(2).content()).isEqualTo("댓글1");
    }

    @Test
    @DisplayName("커서 기반 조회 - createdAt & idCursor 기준")
    void findByFeedIdWithCursor_withCursor() {
        FeedComment latest = em.getEntityManager()
            .createQuery("select c from FeedComment c where c.content = :content", FeedComment.class)
            .setParameter("content", "댓글3")
            .getSingleResult();

        Instant cursorCreatedAt = latest.getCreatedAt();
        UUID cursorId = latest.getId();

        List<CommentDto> results = feedCommentRepositoryImpl.findByFeedIdWithCursor(feedId, cursorCreatedAt, cursorId, 2);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).content()).isEqualTo("댓글2");
        assertThat(results.get(1).content()).isEqualTo("댓글1");
    }
}