package com.sprint.ootd5team.directmessage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sprint.ootd5team.domain.directmessage.dto.DirectMessageCreateRequest;
import com.sprint.ootd5team.domain.directmessage.dto.event.DirectMessageCommittedEvent;
import com.sprint.ootd5team.domain.directmessage.entity.DirectMessage;
import com.sprint.ootd5team.domain.directmessage.entity.DirectMessageRoom;
import com.sprint.ootd5team.domain.directmessage.repository.DirectMessageRepository;
import com.sprint.ootd5team.domain.directmessage.repository.DirectMessageRoomRepository;
import com.sprint.ootd5team.domain.directmessage.service.DirectMessageWsService;
import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DirectMessageWsServiceTest {

    @Mock
    private DirectMessageRepository messageRepository;

    @Mock
    private DirectMessageRoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ProfileRepository profileRepository;

    private ObjectMapper objectMapper;
    private Cache<UUID, String> userNameCache;
    private Cache<UUID, Optional<String>> profileUrlCache;
    private Cache<String, DirectMessageRoom> roomCache;

    private DirectMessageWsService wsService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        userNameCache = Caffeine.newBuilder().build();
        profileUrlCache = Caffeine.newBuilder().build();
        roomCache = Caffeine.newBuilder().build();

        wsService = new DirectMessageWsService(
            objectMapper,
            messageRepository,
            roomRepository,
            userRepository,
            eventPublisher,
            profileRepository,
            userNameCache,
            profileUrlCache,
            roomCache
        );
    }

    @Test
    @DisplayName("1. 성공: 메세지Send호출시 메시지를 저장하고 이벤트를 발행")
    void handleSend_success() throws Exception {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        String content = "hello";
        DirectMessageCreateRequest request = new DirectMessageCreateRequest(senderId, receiverId, content);
        String payload = objectMapper.writeValueAsString(request);

        String dmKey = senderId.toString().compareTo(receiverId.toString()) <= 0
            ? senderId + "_" + receiverId
            : receiverId + "_" + senderId;

        when(roomRepository.findByDmKey(dmKey)).thenReturn(Optional.empty());
        UUID roomId = UUID.randomUUID();
        when(roomRepository.save(any(DirectMessageRoom.class))).thenAnswer(invocation -> {
            DirectMessageRoom room = invocation.getArgument(0);
            ReflectionTestUtils.setField(room, "id", roomId);
            return room;
        });

        UUID messageId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
        when(messageRepository.save(any(DirectMessage.class))).thenAnswer(invocation -> {
            DirectMessage message = invocation.getArgument(0);
            ReflectionTestUtils.setField(message, "id", messageId);
            ReflectionTestUtils.setField(message, "createdAt", createdAt);
            return message;
        });

        User sender = new User("sender", "sender@test.com", "pw", Role.USER);
        ReflectionTestUtils.setField(sender, "id", senderId);
        User receiver = new User("receiver", "receiver@test.com", "pw", Role.USER);
        ReflectionTestUtils.setField(receiver, "id", receiverId);
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));

        Profile senderProfile = new Profile(sender, "sender", null, null, "http://sender", null, null);
        Profile receiverProfile = new Profile(receiver, "receiver", null, null, "http://receiver",null, null);
        when(profileRepository.findByUserId(senderId)).thenReturn(Optional.of(senderProfile));
        when(profileRepository.findByUserId(receiverId)).thenReturn(Optional.of(receiverProfile));

        wsService.handleSend(payload);

        verify(roomRepository).findByDmKey(dmKey);
        verify(roomRepository).save(any(DirectMessageRoom.class));
        verify(messageRepository).save(any(DirectMessage.class));
        verify(userRepository).findById(senderId);
        verify(userRepository).findById(receiverId);
        verify(profileRepository).findByUserId(senderId);
        verify(profileRepository).findByUserId(receiverId);

        ArgumentCaptor<DirectMessageCommittedEvent> eventCaptor = ArgumentCaptor.forClass(DirectMessageCommittedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        DirectMessageCommittedEvent event = eventCaptor.getValue();
        assertThat(event.destination()).isEqualTo("/sub/direct-messages_" + dmKey);
        assertThat(event.payload().id()).isEqualTo(messageId);
        assertThat(event.payload().createdAt()).isEqualTo(createdAt);
        assertThat(event.payload().content()).isEqualTo(content);
        assertThat(event.payload().sender().userId()).isEqualTo(senderId);
        assertThat(event.payload().sender().profileImageUrl()).isEqualTo("http://sender");
        assertThat(event.payload().receiver().userId()).isEqualTo(receiverId);
        assertThat(event.payload().receiver().profileImageUrl()).isEqualTo("http://receiver");
    }

    @Test
    @DisplayName("2. 실패: DM 채팅방의 참가자가 아니면 예외 발생")
    void handleSend_accessDenied() throws Exception {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        DirectMessageCreateRequest request = new DirectMessageCreateRequest(senderId, receiverId, "hello");
        String payload = objectMapper.writeValueAsString(request);

        String dmKey = senderId.toString().compareTo(receiverId.toString()) <= 0
            ? senderId + "_" + receiverId
            : receiverId + "_" + senderId;

        DirectMessageRoom room = DirectMessageRoom.builder()
            .dmKey(dmKey)
            .user1Id(UUID.randomUUID())
            .user2Id(UUID.randomUUID())
            .build();
        ReflectionTestUtils.setField(room, "id", UUID.randomUUID());
        when(roomRepository.findByDmKey(dmKey)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> wsService.handleSend(payload))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("채팅방 멤버가 아닙니다.");

        verify(messageRepository, never()).save(any(DirectMessage.class));
        verify(eventPublisher, never()).publishEvent(any(Object.class));
        verify(userRepository, never()).findById(any(UUID.class));
        verify(profileRepository, never()).findByUserId(any(UUID.class));
        verify(roomRepository, never()).save(any(DirectMessageRoom.class));
    }
}