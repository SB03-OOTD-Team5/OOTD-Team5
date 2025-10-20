package com.sprint.ootd5team.domain.notification.publishing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.sprint.ootd5team.domain.clothesattribute.dto.ClothesAttributeDefDto;
import com.sprint.ootd5team.domain.comment.dto.data.CommentDto;
import com.sprint.ootd5team.domain.directmessage.dto.DirectMessageDto;
import com.sprint.ootd5team.domain.directmessage.dto.ParticipantDto;
import com.sprint.ootd5team.domain.follow.dto.data.FollowDto;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import com.sprint.ootd5team.domain.notification.event.type.multi.ClothesAttributeCreatedEvent;
import com.sprint.ootd5team.domain.notification.event.type.multi.ClothesAttributeUpdatedEvent;
import com.sprint.ootd5team.domain.notification.event.type.multi.FeedCreatedEvent;
import com.sprint.ootd5team.domain.notification.event.type.single.CommentCreatedEvent;
import com.sprint.ootd5team.domain.notification.event.type.single.DmCreatedEvent;
import com.sprint.ootd5team.domain.notification.event.type.single.FeedLikedEvent;
import com.sprint.ootd5team.domain.notification.event.type.single.FollowCreatedEvent;
import com.sprint.ootd5team.domain.notification.event.type.single.RoleUpdatedEvent;
import com.sprint.ootd5team.domain.user.dto.AuthorDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("도메인 알림 이벤트 발행 테스트")
class NotificationEventPublishingTest {

    @Nested
    @DisplayName("receiverIds(알림 전송 대상자 1) 알림 생성")
    @TestClassOrder(ClassOrderer.DisplayName.class)
    class SingleReceiverEvent {
        private UUID receiverId = UUID.randomUUID();

        @Test
        void createRoleUpdatedEvent_알림_이벤트() {
            RoleUpdatedEvent event = new RoleUpdatedEvent(receiverId, "USER", "ADMIN");

            assertThat(event.getReceiverIds())
                .containsExactlyInAnyOrder(receiverId);
            assertThat(event.getTemplateType()).isEqualTo(NotificationTemplateType.ROLE_UPDATED);
            assertThat(event.getData().oldRole()).isEqualTo("USER");
            assertThat(event.getData().newRole()).isEqualTo("ADMIN");
            assertThat(event.getArgs()).containsExactly("USER", "ADMIN");
        }

        @Test
        void createCommentCreatedEvent_알림_이벤트() {
            UUID feedId = UUID.randomUUID();
            UUID commentId = UUID.randomUUID();

            CommentDto payload = new CommentDto(
                feedId,
                Instant.now(),
                commentId,
                mock(AuthorDto.class),
                "댓글 내용");

            CommentCreatedEvent event = new CommentCreatedEvent(payload, receiverId);

            assertThat(event.getReceiverIds())
                .containsExactlyInAnyOrder(receiverId);
            assertThat(event.getTemplateType()).isEqualTo(NotificationTemplateType.FEED_COMMENTED);
            assertThat(event.getData().content()).isEqualTo("댓글 내용");
            assertThat(event.getArgs()).contains("댓글 내용");
        }

        @Test
        void FeedLikedEven_알림_이벤트() {
            UUID feedId = UUID.randomUUID();

            FeedLikedEvent event = new FeedLikedEvent(feedId, receiverId, "피드 내용", "좋아요한사람");

            assertThat(event.getReceiverIds())
                .containsExactlyInAnyOrder(receiverId);
            assertThat(event.getTemplateType()).isEqualTo(NotificationTemplateType.FEED_LIKED);
            assertThat(event.getData().likerName()).isEqualTo("좋아요한사람");
            assertThat(event.getData().feedContent()).isEqualTo("피드 내용");
            assertThat(event.getArgs()).containsExactly("좋아요한사람", "피드 내용");
        }

        @Test
        @DisplayName("FollowCreatedEvent - 알림이벤트 생성 검증")
        void FollowCreatedEvent_알림_이벤트() {
            // given
            AuthorDto receiver = new AuthorDto(receiverId, "팔로위", "profile.png");
            AuthorDto follower = new AuthorDto(UUID.randomUUID(), "팔로워", "profile.png");
            FollowDto payload = new FollowDto(UUID.randomUUID(), receiver, follower);

            // when
            FollowCreatedEvent event = new FollowCreatedEvent(payload);

            // then
            assertThat(event.getReceiverIds()).containsExactly(receiverId);
            assertThat(event.getTemplateType()).isEqualTo(NotificationTemplateType.FOLLOWED);
            assertThat(event.getData().follower().name()).isEqualTo("팔로워");
            assertThat(event.getArgs()).containsExactly("팔로워");
        }

        @Test
        void DmCreatedEvent_알림_이벤트() {
            UUID messageId = UUID.randomUUID();

            ParticipantDto sender = ParticipantDto.builder().userId(UUID.randomUUID()).name("sender").build();
            ParticipantDto receiver = ParticipantDto.builder().userId(receiverId).name("receiver").build();

            DirectMessageDto payload =
                new DirectMessageDto(messageId, Instant.now(), sender, receiver, "하이루");

            DmCreatedEvent event = new DmCreatedEvent(payload);

            assertThat(event.getReceiverIds())
                .containsExactlyInAnyOrder(receiverId);
            assertThat(event.getTemplateType()).isEqualTo(NotificationTemplateType.DM_RECEIVED);
            assertThat(event.getData().content()).isEqualTo("하이루");
            assertThat(event.getArgs()).containsExactly("sender", "하이루");
        }

    }

    @Nested
    @DisplayName("receiverIds(알림 전송 대상자 n) 알림 생성")
    @TestClassOrder(ClassOrderer.DisplayName.class)
    class MultiReceiverEvent {

        private List<UUID> receiverIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        @Test
        void FeedCreatedEvent_알림_이벤트() {
            UUID feedId = UUID.randomUUID();
            UUID authorId = UUID.randomUUID();

            FeedCreatedEvent event = new FeedCreatedEvent(feedId, authorId, "작성자", "내용", receiverIds);

            assertThat(event.getReceiverIds()).isEqualTo(receiverIds);  // 특정 다수 사용자
            assertThat(event.getTemplateType()).isEqualTo(
                NotificationTemplateType.FEED_FOLLOW_CREATED);
            assertThat(event.getData().authorName()).isEqualTo("작성자");
            assertThat(event.getArgs()).containsExactly("작성자", "내용");
        }

        @Test
        void ClothesAttributeCreatedEvent_알림_이벤트() {
            ClothesAttributeDefDto payload = new ClothesAttributeDefDto(
                UUID.randomUUID(),
                "날씨",
                List.of("봄, 여름, 가을, 겨울"),
                Instant.now()
            );

            ClothesAttributeCreatedEvent event =
                new ClothesAttributeCreatedEvent(payload);

            assertThat(event.getReceiverIds()).isNullOrEmpty();      // Consumer 에서 처리
            assertThat(event.getTemplateType()).isEqualTo(
                NotificationTemplateType.CLOTHES_ATTRIBUTE_CREATED);
            assertThat(event.getData().name()).isEqualTo("날씨");
            assertThat(event.getArgs()).containsExactly("날씨");
        }

        @Test
        void ClothesAttributeUpdatedEvent_알림_이벤트() {
            ClothesAttributeDefDto payload = new ClothesAttributeDefDto(
                UUID.randomUUID(),
                "날씨",
                List.of("봄, 여름, 가을, 겨울"),
                Instant.now()
            );

            ClothesAttributeUpdatedEvent event = new ClothesAttributeUpdatedEvent(payload);

            assertThat(event.getReceiverIds()).isNullOrEmpty();
            assertThat(event.getTemplateType()).isEqualTo(
                NotificationTemplateType.CLOTHES_ATTRIBUTE_UPDATED);
            assertThat(event.getData().name()).isEqualTo("날씨");
            assertThat(event.getArgs()).containsExactly("날씨");
        }
    }

    @Test
    void 템플릿_변환_검증_followCreatedEvent() {
        // given
        AuthorDto receiver = new AuthorDto(UUID.randomUUID(), "팔로위", "profile.png");
        AuthorDto follower = new AuthorDto(UUID.randomUUID(), "팔로워", "profile.png");
        FollowDto payload = new FollowDto(UUID.randomUUID(), receiver, follower);
        FollowCreatedEvent event = new FollowCreatedEvent(payload);

        // when
        NotificationTemplateType type = event.getTemplateType();
        String title = type.formatTitle(event.getArgs());
        String content = type.formatContent(event.getArgs());

        // then
        assertThat(title).isEqualTo("팔로워님이 나를 팔로우 했어요");
        assertThat(content).isEqualTo("");
    }
}

