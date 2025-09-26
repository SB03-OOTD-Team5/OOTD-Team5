package com.sprint.ootd5team.domain.follow.mapper;

import com.sprint.ootd5team.domain.follow.dto.data.FollowDto;
import com.sprint.ootd5team.domain.follow.dto.data.FollowProjectionDto;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FollowMapper {

    List<FollowDto> toFollowDtoList(List<FollowProjectionDto> projections);
}