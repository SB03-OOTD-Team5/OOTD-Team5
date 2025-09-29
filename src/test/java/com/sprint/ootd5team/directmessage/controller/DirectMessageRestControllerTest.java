package com.sprint.ootd5team.directmessage.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.ootd5team.domain.directmessage.controller.DirectMessageRestController;
import com.sprint.ootd5team.domain.directmessage.dto.DirectMessageDto;
import com.sprint.ootd5team.domain.directmessage.dto.DirectMessageDtoCursorResponse;
import com.sprint.ootd5team.domain.directmessage.dto.ParticipantDto;
import com.sprint.ootd5team.domain.directmessage.service.DirectMessageRestService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
@DisplayName("DirectMessageRestController 테스트")
@TestClassOrder(ClassOrderer.DisplayName.class)
class DirectMessageRestControllerTest {

	@Mock
	private DirectMessageRestService restService;

	@InjectMocks
	private DirectMessageRestController controller;

	@Test
	@DisplayName("1. 성공: Service에 과거대화내역을 요청, 결과 반환")
	void list_success() {
		UUID userId = UUID.randomUUID();
		String cursor = "2024-01-01T00:00:00Z";
		UUID idAfter = UUID.randomUUID();
		int limit = 30;

		ParticipantDto sender = ParticipantDto.builder().userId(UUID.randomUUID()).name("sender").build();
		ParticipantDto receiver = ParticipantDto.builder().userId(UUID.randomUUID()).name("receiver").build();
		DirectMessageDto dto = DirectMessageDto.builder()
			.id(UUID.randomUUID())
			.createdAt(Instant.now())
			.sender(sender)
			.receiver(receiver)
			.content("hello")
			.build();
		DirectMessageDtoCursorResponse expected = DirectMessageDtoCursorResponse.builder()
			.data(List.of(dto))
			.nextCursor("next")
			.nextIdAfter(UUID.randomUUID())
			.hasNext(true)
			.totalCount(10)
			.sortBy("createdAt")
			.sortDirection("DESCENDING")
			.build();

		when(restService.listByPartner(userId, cursor, idAfter, limit)).thenReturn(expected);

		DirectMessageDtoCursorResponse actual = controller.list(userId, cursor, idAfter, limit);

		assertThat(actual).isEqualTo(expected);
		verify(restService).listByPartner(userId, cursor, idAfter, limit);
	}

	@Test
	@DisplayName("2. 실패: AccessDeniedException 발생시 그대로 전파")
	void list_propagatesException() {
		UUID userId = UUID.randomUUID();

		when(restService.listByPartner(userId, null, null, 20)).thenThrow(new AccessDeniedException("denied"));

		assertThatThrownBy(() -> controller.list(userId, null, null, 20))
			.isInstanceOf(AccessDeniedException.class)
			.hasMessage("denied");
	}
}
