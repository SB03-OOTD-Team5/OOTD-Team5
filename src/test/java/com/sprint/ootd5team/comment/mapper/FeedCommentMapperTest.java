package com.sprint.ootd5team.comment.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.ootd5team.domain.comment.dto.data.CommentDto;
import com.sprint.ootd5team.domain.comment.entity.FeedComment;
import com.sprint.ootd5team.domain.comment.mapper.FeedCommentMapper;
import com.sprint.ootd5team.domain.comment.mapper.FeedCommentMapperImpl;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.mapper.ProfileMapper;
import com.sprint.ootd5team.domain.profile.mapper.ProfileMapperImpl;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("FeedCommentMapper 단위 테스트")
class FeedCommentMapperTest {

    private final FeedCommentMapper mapper;

    FeedCommentMapperTest() throws Exception {
        FeedCommentMapperImpl impl = new FeedCommentMapperImpl();

        ProfileMapper profileMapper = new ProfileMapperImpl();
        Field field = FeedCommentMapperImpl.class.getDeclaredField("profileMapper");
        field.setAccessible(true);
        field.set(impl, profileMapper);

        this.mapper = impl;
    }

    @Test
    void toDto_success() {
        UUID feedId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        User user = new User("user","user@test.com","pw", Role.USER);
        ReflectionTestUtils.setField(user, "id", authorId);

        FeedComment comment = new FeedComment(feedId, authorId, "테스트 댓글");

        Profile profile = new Profile();
        ReflectionTestUtils.setField(profile, "user", user);
        ReflectionTestUtils.setField(profile, "name", "테스터");

        CommentDto dto = mapper.toDto(comment, profile);

        assertThat(dto.feedId()).isEqualTo(feedId);
        assertThat(dto.content()).isEqualTo("테스트 댓글");
        assertThat(dto.author().userId()).isEqualTo(authorId);
    }
}