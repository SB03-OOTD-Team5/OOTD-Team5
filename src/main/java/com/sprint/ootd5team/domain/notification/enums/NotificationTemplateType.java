package com.sprint.ootd5team.domain.notification.enums;

import lombok.Getter;

@Getter
public enum NotificationTemplateType {
    // 권한
    ROLE_CHANGED("내 권한이 변경되었어요.", "내 권한이 [%s]에서 [%s]로 변경 되었어요."),

    // 피드
    FEED_LIKED("%s님이 내 피드를 좋아합니다.", "%s"),
    FEED_FOLLOW_CREATED("%s님이 새로운 피드를 작성했어요.", "%s"),
    FEED_COMMENTED("%s님이 댓글을 달았어요.", "%s"),

    // 팔로우
    FOLLOWED("팔로우", ""),

    // DM
    DM_RECEIVED("[DM] %s", "%s"),

    // 의상 속성
    CLOTHES_ATTRIBUTE_ADDED("새로운 의상 속성이 추가되었어요.", "내 의상에 [%s]속성을 추가해보세요."),
    CLOTHES_ATTRIBUTE_UPDATE("의상 속성이 변경되었어요.", "[%s]속성을 확인해보세요.");

    private final String titleTemplate;
    private final String contentTemplate;

    NotificationTemplateType(String titleTemplate, String contentTemplate) {
        this.titleTemplate = titleTemplate;
        this.contentTemplate = contentTemplate;
    }

    public String formatTitle(Object... args) {
        return String.format(titleTemplate, args);
    }

    public String formatContent(Object... args) {
        return String.format(contentTemplate, args);
    }
}
