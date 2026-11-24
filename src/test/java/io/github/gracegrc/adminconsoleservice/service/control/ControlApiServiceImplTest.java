package io.github.gracegrc.adminconsoleservice.service.control;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.gracegrc.adminconsoleservice.boot.client.ControlApiClient;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlGroupRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlGroupUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlUpdateRequestDto;
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

	private static final UUID GROUP_ID = UUID.fromString("6e7f8091-2b3c-4d4e-9f50-6b7c8d9e0f1a");

	private static final UUID PARENT_GROUP_ID = UUID.fromString("5d6e7f80-1a2b-4c3d-8e4f-5a6b7c8d9e0f");

	private static final UUID CONTROL_ID = UUID.fromString("80910213-4d5e-4f60-b172-8d9e0f1a2b3c");

	private static final UUID PARENT_CONTROL_ID = UUID.fromString("7f809102-3c4d-4e5f-a061-7c8d9e0f1a2b");

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

	@Test
	void createControlGroupSendsTheGroupWithCreatedByAndReturnsTheNewId() {
		given(controlApiClient.createControlGroup(
				new ControlApiControlGroupRequestDto(PARENT_GROUP_ID, "AC-1", "Policy", "admin-console")))
			.willReturn(GROUP_ID);

		assertThat(controlApiService.createControlGroup(new ControlGroupRequestDto(PARENT_GROUP_ID, "AC-1", "Policy")))
			.isEqualTo(GROUP_ID);
	}

	@Test
	void updateControlGroupSendsEveryField() {
		controlApiService.updateControlGroup(GROUP_ID, new ControlGroupUpdateRequestDto(null, "AC", "Access Control"));

		then(controlApiClient).should()
			.updateControlGroup(GROUP_ID, new ControlApiControlGroupUpdateRequestDto(null, "AC", "Access Control"));
	}

	@Test
	void deleteControlGroupDeletesItThroughTheClient() {
		controlApiService.deleteControlGroup(GROUP_ID);

		then(controlApiClient).should().deleteControlGroup(GROUP_ID);
	}

	@Test
	void createControlSendsTheControlWithCreatedByAndReturnsTheNewId() {
		given(controlApiClient.createControl(new ControlApiControlRequestDto(GROUP_ID, PARENT_CONTROL_ID, "AC-2(1)",
				"Automated Account Management", "Support the management...", "admin-console")))
			.willReturn(CONTROL_ID);

		assertThat(controlApiService.createControl(new ControlRequestDto(GROUP_ID, PARENT_CONTROL_ID, "AC-2(1)",
				"Automated Account Management", "Support the management...")))
			.isEqualTo(CONTROL_ID);
	}

	@Test
	void updateControlSendsEveryField() {
		controlApiService.updateControl(CONTROL_ID,
				new ControlUpdateRequestDto(GROUP_ID, null, "AC-2", "Account Management", null));

		then(controlApiClient).should()
			.updateControl(CONTROL_ID,
					new ControlApiControlUpdateRequestDto(GROUP_ID, null, "AC-2", "Account Management", null));
	}

	@Test
	void deleteControlDeletesItThroughTheClient() {
		controlApiService.deleteControl(CONTROL_ID);

		then(controlApiClient).should().deleteControl(CONTROL_ID);
	}

}
