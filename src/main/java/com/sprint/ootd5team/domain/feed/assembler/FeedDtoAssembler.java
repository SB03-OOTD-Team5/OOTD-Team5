package com.sprint.ootd5team.domain.feed.assembler;

import com.sprint.ootd5team.base.storage.FileStorage;
import com.sprint.ootd5team.domain.feed.dto.data.FeedDto;
import com.sprint.ootd5team.domain.feed.dto.data.OotdDto;
import com.sprint.ootd5team.domain.feed.repository.feedClothes.FeedClothesRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * FeedDto 후처리를 담당하는 조립기
 * <p>
 * - OOTD 목록 매핑
 * - 프로필 이미지 S3 URL 변환
 */
@Component
@RequiredArgsConstructor
public class FeedDtoAssembler {

    private final FeedClothesRepository feedClothesRepository;
    private final FileStorage fileStorage;

    public List<FeedDto> enrich(List<FeedDto> feedDtos) {
        Map<UUID, List<OotdDto>> ootdsMap =
            feedClothesRepository.findOotdsByFeedIds(feedDtos.stream().map(FeedDto::id).toList());

        return feedDtos.stream()
            .map(feedDto -> feedDto
                .withOotds(
                    ootdsMap.getOrDefault(feedDto.id(), List.of())
                        .stream()
                        .map(ootdDto -> ootdDto.withResolvedImageUrl(
                            fileStorage.resolveUrl(ootdDto.imageUrl())
                        ))
                        .toList()
                    )
                .withResolvedProfileImageUrl(fileStorage.resolveUrl(feedDto.author().profileImageUrl()))
            ).toList();
    }
}