package com.sprint.ootd5team.domain.directmessage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.ootd5team.base.exception.directmessage.DirectMessageAccessDeniedException;
import com.sprint.ootd5team.domain.directmessage.dto.DirectMessageDto;
import com.sprint.ootd5team.domain.directmessage.dto.DirectMessageDtoCursorResponse;
import com.sprint.ootd5team.domain.directmessage.entity.DirectMessage;
import com.sprint.ootd5team.domain.directmessage.entity.DirectMessageRoom;
import com.sprint.ootd5team.domain.directmessage.repository.DirectMessageRepository;
import com.sprint.ootd5team.domain.directmessage.repository.DirectMessageRoomRepository;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("DirectMessageRestService 테스트")
@TestClassOrder(ClassOrderer.DisplayName.class)
class DirectMessageRestServiceTest {

    @Mock
    private DirectMessageRepository messageRepository;

    @Mock
    private DirectMessageRoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    private DirectMessageRestService directMessageRestService;

    @BeforeEach
    void setUp() {
        directMessageRestService = new DirectMessageRestService(
            messageRepository,
            roomRepository,
            userRepository,
            profileRepository
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("1. 성공: listByPartner 메서드가 대화내역 반환")
    void listByPartner_success() {
        // given
        UUID currentUserId = UUID.randomUUID();
        UUID partnerUserId = UUID.randomUUID();
        setAuthentication(currentUserId);

        String dmKey = dmKeyOf(currentUserId, partnerUserId);
        DirectMessageRoom room = DirectMessageRoom.builder()
            .dmKey(dmKey)
            .user1Id(currentUserId)
            .user2Id(partnerUserId)
            .build();
        UUID roomId = UUID.randomUUID();
        ReflectionTestUtils.setField(room, "id", roomId);

        DirectMessage message1 = createMessage(room, currentUserId, "Hello", Instant.parse("2024-01-03T00:00:00Z"));
        DirectMessage message2 = createMessage(room, partnerUserId, "Hi", Instant.parse("2024-01-02T00:00:00Z"));
        DirectMessage message3 = createMessage(room, currentUserId, "How are you?", Instant.parse("2024-01-01T00:00:00Z"));

        when(roomRepository.findByDmKey(dmKey)).thenReturn(Optional.of(room));
        when(messageRepository.firstPageDesc(eq(roomId), ArgumentMatchers.any(Pageable.class)))
            .thenReturn(List.of(message1, message2, message3));
        when(messageRepository.countByDirectMessageRoom_Id(roomId)).thenReturn(5L);

        User currentUser = new User("Alice", "alice@example.com", "encoded", Role.USER);
        ReflectionTestUtils.setField(currentUser, "id", currentUserId);
        User partnerUser = new User("Bob", "bob@example.com", "encoded", Role.USER);
        ReflectionTestUtils.setField(partnerUser, "id", partnerUserId);

        when(userRepository.findAllById(ArgumentMatchers.<Iterable<UUID>>any()))
            .thenReturn(List.of(currentUser, partnerUser));
        when(profileRepository.findByUserId(currentUserId)).thenReturn(Optional.empty());
        when(profileRepository.findByUserId(partnerUserId)).thenReturn(Optional.empty());

        int limit = 2;

        // when
        DirectMessageDtoCursorResponse response = directMessageRestService.listByPartner(
            partnerUserId,
            null,
            null,
            limit
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.data()).hasSize(limit);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.nextIdAfter()).isEqualTo(message2.getId());
        assertThat(response.totalCount()).isEqualTo(5L);
        assertThat(response.sortBy()).isEqualTo("createdAt");
        assertThat(response.sortDirection()).isEqualTo("DESCENDING");

        DirectMessageDto first = response.data().get(0);
        assertThat(first.content()).isEqualTo("Hello");
        assertThat(first.sender().userId()).isEqualTo(currentUserId);
        assertThat(first.receiver().userId()).isEqualTo(partnerUserId);

        DirectMessageDto second = response.data().get(1);
        assertThat(second.content()).isEqualTo("Hi");
        assertThat(second.sender().userId()).isEqualTo(partnerUserId);
        assertThat(second.receiver().userId()).isEqualTo(currentUserId);
    }

    @Test
    @DisplayName("2. 실패: 조회 요청자가 해당 DirectMessage 참여자가 아님")
    void listByPartner_accessDenied() {
        // given
        UUID currentUserId = UUID.randomUUID();
        UUID partnerUserId = UUID.randomUUID();
        UUID strangerId = UUID.randomUUID();
        setAuthentication(currentUserId);

        String dmKey = dmKeyOf(currentUserId, partnerUserId);
        DirectMessageRoom room = DirectMessageRoom.builder()
            .dmKey(dmKey)
            .user1Id(partnerUserId)
            .user2Id(strangerId)
            .build();
        ReflectionTestUtils.setField(room, "id", UUID.randomUUID());

        when(roomRepository.findByDmKey(dmKey)).thenReturn(Optional.of(room));

        // when / then
        assertThatThrownBy(() -> directMessageRestService.listByPartner(partnerUserId, null, null, 20))
            .isInstanceOf(DirectMessageAccessDeniedException.class);

        verify(messageRepository, never()).firstPageDesc(any(), any());
    }

    private DirectMessage createMessage(DirectMessageRoom room, UUID senderId, String content, Instant createdAt) {
        DirectMessage message = DirectMessage.builder()
            .directMessageRoom(room)
            .senderId(senderId)
            .content(content)
            .build();
        ReflectionTestUtils.setField(message, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(message, "createdAt", createdAt);
        return message;
    }

    private void setAuthentication(UUID userId) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userId.toString(),
            null,
            Collections.emptyList()
        );
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    private String dmKeyOf(UUID a, UUID b) {
        String s1 = a.toString();
        String s2 = b.toString();
        return (s1.compareTo(s2) <= 0) ? s1 + "_" + s2 : s2 + "_" + s1;
    }
}
