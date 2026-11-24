package io.github.gracegrc.adminconsoleservice.service.control;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.gracegrc.adminconsoleservice.boot.client.ControlMappingApiClient;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlMappingRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlMappingUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingUpdateRequestDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ControlMappingApiServiceImplTest {

	private static final UUID REQUIREMENT_ID = UUID.fromString("5d6e7f80-1a2b-4c3d-8e4f-5a6b7c8d9e0f");

	private static final UUID CONTROL_ID = UUID.fromString("80910213-4d5e-4f60-b172-8d9e0f1a2b3c");

	private static final UUID MAPPING_ID = UUID.fromString("7f809102-3c4d-4e5f-a061-7c8d9e0f1a2b");

	@Mock
	private ControlMappingApiClient controlMappingApiClient;

	@InjectMocks
	private ControlMappingApiServiceImpl controlMappingApiService;

	@Test
	void createMappingSendsTheMappingCreatedByTheAdminConsoleAndReturnsItsId() {
		given(controlMappingApiClient.createMapping(
				new ControlMappingApiMappingRequestDto(REQUIREMENT_ID, CONTROL_ID, "Covers it", "admin-console")))
			.willReturn(MAPPING_ID);

		assertThat(controlMappingApiService
			.createMapping(new ControlMappingRequestDto(REQUIREMENT_ID, CONTROL_ID, "Covers it"))).isEqualTo(MAPPING_ID);
	}

	@Test
	void findAllMappingsReturnsTheMappingsFromTheClient() {
		List<ControlMappingApiMappingResponseDto> mappings = List.of(new ControlMappingApiMappingResponseDto(
				UUID.randomUUID(), REQUIREMENT_ID, UUID.randomUUID(), null, Instant.parse("2026-10-01T10:15:30Z"), "me"));
		given(controlMappingApiClient.findAllMappings()).willReturn(mappings);

		assertThat(controlMappingApiService.findAllMappings()).isEqualTo(mappings);
	}

	@Test
	void updateMappingSendsTheRationale() {
		controlMappingApiService.updateMapping(MAPPING_ID, new ControlMappingUpdateRequestDto("Now covers it"));

		then(controlMappingApiClient).should()
			.updateMapping(MAPPING_ID, new ControlMappingApiMappingUpdateRequestDto("Now covers it"));
	}

	@Test
	void deleteMappingDeletesItThroughTheClient() {
		controlMappingApiService.deleteMapping(MAPPING_ID);

		then(controlMappingApiClient).should().deleteMapping(MAPPING_ID);
	}

}
