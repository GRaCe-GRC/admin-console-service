package io.github.gracegrc.adminconsoleservice.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlGroupRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlGroupUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupUpdateRequestDto;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ControlGroupMapperTest {

	private static final UUID PARENT_GROUP_ID = UUID.fromString("5d6e7f80-1a2b-4c3d-8e4f-5a6b7c8d9e0f");

	@Test
	void toControlApiRequestCopiesTheFieldsAndAddsCreatedBy() {
		ControlApiControlGroupRequestDto request = ControlGroupMapper
			.toControlApiRequest(new ControlGroupRequestDto(PARENT_GROUP_ID, "AC", "Access Control"), "admin-console");

		assertThat(request).isEqualTo(
				new ControlApiControlGroupRequestDto(PARENT_GROUP_ID, "AC", "Access Control", "admin-console"));
	}

	@Test
	void toControlApiRequestKeepsAMissingParentGroup() {
		assertThat(ControlGroupMapper.toControlApiRequest(new ControlGroupRequestDto(null, "AC", "Access Control"),
				"admin-console"))
			.isEqualTo(new ControlApiControlGroupRequestDto(null, "AC", "Access Control", "admin-console"));
	}

	@Test
	void toControlApiUpdateRequestCopiesEveryField() {
		ControlApiControlGroupUpdateRequestDto request = ControlGroupMapper
			.toControlApiUpdateRequest(new ControlGroupUpdateRequestDto(PARENT_GROUP_ID, "AC", "Access Control"));

		assertThat(request)
			.isEqualTo(new ControlApiControlGroupUpdateRequestDto(PARENT_GROUP_ID, "AC", "Access Control"));
	}

}
