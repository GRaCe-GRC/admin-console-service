package io.github.gracegrc.adminconsoleservice.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlUpdateRequestDto;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ControlMapperTest {

	private static final UUID GROUP_ID = UUID.fromString("6e7f8091-2b3c-4d4e-9f50-6b7c8d9e0f1a");

	private static final UUID PARENT_CONTROL_ID = UUID.fromString("7f809102-3c4d-4e5f-a061-7c8d9e0f1a2b");

	@Test
	void toControlApiRequestCopiesTheFieldsAndAddsCreatedBy() {
		ControlApiControlRequestDto request = ControlMapper.toControlApiRequest(new ControlRequestDto(GROUP_ID,
				PARENT_CONTROL_ID, "AC-2(1)", "Automated Account Management", "Support the management..."),
				"admin-console");

		assertThat(request).isEqualTo(new ControlApiControlRequestDto(GROUP_ID, PARENT_CONTROL_ID, "AC-2(1)",
				"Automated Account Management", "Support the management...", "admin-console"));
	}

	@Test
	void toControlApiUpdateRequestCopiesEveryFieldIncludingNulls() {
		ControlApiControlUpdateRequestDto request = ControlMapper
			.toControlApiUpdateRequest(new ControlUpdateRequestDto(null, PARENT_CONTROL_ID, "AC-2(1)",
					"Automated Account Management", null));

		assertThat(request).isEqualTo(new ControlApiControlUpdateRequestDto(null, PARENT_CONTROL_ID, "AC-2(1)",
				"Automated Account Management", null));
	}

}
