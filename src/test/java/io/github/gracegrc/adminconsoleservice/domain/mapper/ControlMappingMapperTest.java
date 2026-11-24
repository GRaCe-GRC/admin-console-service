package io.github.gracegrc.adminconsoleservice.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlMappingRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlMappingUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingUpdateRequestDto;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ControlMappingMapperTest {

	private static final UUID REQUIREMENT_ID = UUID.fromString("5d6e7f80-1a2b-4c3d-8e4f-5a6b7c8d9e0f");

	private static final UUID CONTROL_ID = UUID.fromString("80910213-4d5e-4f60-b172-8d9e0f1a2b3c");

	@Test
	void toControlMappingApiRequestCopiesTheFieldsAndAddsCreatedBy() {
		ControlMappingApiMappingRequestDto request = ControlMappingMapper.toControlMappingApiRequest(
				new ControlMappingRequestDto(REQUIREMENT_ID, CONTROL_ID, "Covers it"), "admin-console");

		assertThat(request).isEqualTo(
				new ControlMappingApiMappingRequestDto(REQUIREMENT_ID, CONTROL_ID, "Covers it", "admin-console"));
	}

	@Test
	void toControlMappingApiRequestKeepsAMissingRationale() {
		ControlMappingApiMappingRequestDto request = ControlMappingMapper.toControlMappingApiRequest(
				new ControlMappingRequestDto(REQUIREMENT_ID, CONTROL_ID, null), "admin-console");

		assertThat(request.rationale()).isNull();
	}

	@Test
	void toControlMappingApiUpdateRequestCopiesTheRationale() {
		assertThat(ControlMappingMapper
			.toControlMappingApiUpdateRequest(new ControlMappingUpdateRequestDto("Now covers it")))
			.isEqualTo(new ControlMappingApiMappingUpdateRequestDto("Now covers it"));
	}

}
