package com.sprint.ootd5team.domain.comment.mapper;

import com.sprint.ootd5team.domain.comment.dto.data.CommentDto;
import com.sprint.ootd5team.domain.comment.entity.FeedComment;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.mapper.ProfileMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProfileMapper.class})
public interface FeedCommentMapper  {

    @Mapping(source = "comment.id", target = "id")
    @Mapping(source = "comment.createdAt", target = "createdAt")
    @Mapping(source = "comment.feedId", target = "feedId")
    @Mapping(source = "profile", target = "author")
    @Mapping(source = "comment.content", target = "content")
    CommentDto toDto(FeedComment comment, Profile profile);
}