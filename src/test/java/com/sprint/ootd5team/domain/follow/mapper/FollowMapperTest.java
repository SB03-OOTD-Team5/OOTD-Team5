package com.sprint.ootd5team.domain.follow.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.ootd5team.domain.follow.dto.data.FollowDto;
import com.sprint.ootd5team.domain.follow.dto.data.FollowProjectionDto;
import com.sprint.ootd5team.domain.follow.mapper.FollowMapper;
import com.sprint.ootd5team.domain.user.dto.AuthorDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowMapper 단위 테스트")
public class FollowMapperTest {

    private final FollowMapper followMapper = Mappers.getMapper(FollowMapper.class);

    private FollowProjectionDto projection;

    @BeforeEach
    void setUp() {
        AuthorDto followee = new AuthorDto(UUID.randomUUID(), "user1", "https://example.com/a.png");
        AuthorDto follower = new AuthorDto(UUID.randomUUID(), "user2", "https://example.com/b.png");

        projection = new FollowProjectionDto(
            UUID.randomUUID(),
            Instant.now(),
            followee,
            follower
        );
    }

    @Test
    @DisplayName("FollowProjectionDto 리스트 → FollowDto 리스트 변환 성공")
    void toFollowDtoList() {
        List<FollowDto> result = followMapper.toFollowDtoList(List.of(projection));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).followee().name()).isEqualTo("user1");
        assertThat(result.get(0).follower().name()).isEqualTo("user2");
    }
}