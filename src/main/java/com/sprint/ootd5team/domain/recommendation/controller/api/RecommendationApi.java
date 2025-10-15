package com.sprint.ootd5team.domain.recommendation.controller.api;

import com.sprint.ootd5team.domain.recommendation.dto.RecommendationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "추천 관리", description = "코디 추천 관련 API")
public interface RecommendationApi {

    @Operation(
        summary = "코디 추천 조회",
        description = """
            사용자의 프로필과 날씨 정보를 기반으로 추천 코디를 반환합니다.<br>
            <ul>
              <li><b>useAi=true</b> → LLM 기반 추천 (AI 조합)</li>
              <li><b>useAi=false</b> → 내부 알고리즘 기반 추천</li>
            </ul>
            """,
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "추천 조회 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = RecommendationDto.class))),
        @ApiResponse(responseCode = "400", description = "요청 파라미터 오류",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "프로필 또는 날씨 정보 없음",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    ResponseEntity<RecommendationDto> getRecommendation(
        @Parameter(
            in = ParameterIn.QUERY,
            description = "추천에 사용할 날씨 ID (UUID)",
            example = "550e8400-e29b-41d4-a716-446655440000",
            required = true
        )
        @RequestParam UUID weatherId,

        @Parameter(
            in = ParameterIn.QUERY,
            description = "AI 기반 추천 여부 (기본값: false)",
            example = "true"
        )
        @RequestParam(required = false, defaultValue = "false") boolean useAi);
}
