package com.sprint.ootd5team.domain.feed.search;

import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Elasticsearch 인덱스에 저장되는 피드 문서 모델.
 *
 * <p>인덱스명: {@code feeds-v5}</p>
 */
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@Document(indexName = "feeds-v1")
public class FeedDocument {

    @Id
    private UUID feedId;

    @Field(type = FieldType.Text, analyzer = "korean_nori_custom", searchAnalyzer = "ngram_analyzer")
    private String content;

    @Field(type = FieldType.Long)
    private long likeCount;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant createdAt;

}