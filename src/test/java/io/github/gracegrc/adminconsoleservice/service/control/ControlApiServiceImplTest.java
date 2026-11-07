package io.github.gracegrc.adminconsoleservice.service.control;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import io.github.gracegrc.adminconsoleservice.boot.client.ControlApiClient;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlResponseDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ControlApiServiceImplTest {

	private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");

	@Mock
	private ControlApiClient controlApiClient;

	@InjectMocks
	private ControlApiServiceImpl controlApiService;

	@Test
	void findAllControlGroupsReturnsTheGroupsFromTheClient() {
		List<ControlApiControlGroupResponseDto> groups = List.of(
				new ControlApiControlGroupResponseDto(UUID.randomUUID(), null, "AC", "Access Control", CREATED_AT, "me"));
		given(controlApiClient.findAllControlGroups()).willReturn(groups);

		assertThat(controlApiService.findAllControlGroups()).isEqualTo(groups);
	}

	@Test
	void findAllControlsReturnsTheControlsFromTheClient() {
		List<ControlApiControlResponseDto> controls = List.of(new ControlApiControlResponseDto(UUID.randomUUID(), null,
				null, "AC-2", "Account Management", null, CREATED_AT, "me"));
		given(controlApiClient.findAllControls()).willReturn(controls);

		assertThat(controlApiService.findAllControls()).isEqualTo(controls);
	}

}
