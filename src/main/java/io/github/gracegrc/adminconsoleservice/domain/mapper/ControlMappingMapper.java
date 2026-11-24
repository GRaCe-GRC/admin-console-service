package io.github.gracegrc.adminconsoleservice.domain.mapper;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlMappingRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlMappingUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingUpdateRequestDto;

public final class ControlMappingMapper {

	private ControlMappingMapper() {
	}

	public static ControlMappingApiMappingRequestDto toControlMappingApiRequest(ControlMappingRequestDto request,
			String createdBy) {
		return new ControlMappingApiMappingRequestDto(request.requirementId(), request.controlId(), request.rationale(),
				createdBy);
	}

	public static ControlMappingApiMappingUpdateRequestDto toControlMappingApiUpdateRequest(
			ControlMappingUpdateRequestDto request) {
		return new ControlMappingApiMappingUpdateRequestDto(request.rationale());
	}

}
