package io.github.gracegrc.adminconsoleservice.domain.mapper;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlGroupRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlGroupUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupUpdateRequestDto;

public final class ControlGroupMapper {

	private ControlGroupMapper() {
	}

	public static ControlApiControlGroupRequestDto toControlApiRequest(ControlGroupRequestDto request,
			String createdBy) {
		return new ControlApiControlGroupRequestDto(request.parentGroupId(), request.groupRef(), request.title(),
				createdBy);
	}

	public static ControlApiControlGroupUpdateRequestDto toControlApiUpdateRequest(
			ControlGroupUpdateRequestDto request) {
		return new ControlApiControlGroupUpdateRequestDto(request.parentGroupId(), request.groupRef(),
				request.title());
	}

}
