package io.github.gracegrc.adminconsoleservice.domain.mapper;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlUpdateRequestDto;

public final class ControlMapper {

	private ControlMapper() {
	}

	public static ControlApiControlRequestDto toControlApiRequest(ControlRequestDto request, String createdBy) {
		return new ControlApiControlRequestDto(request.groupId(), request.parentControlId(), request.controlRef(),
				request.title(), request.statement(), createdBy);
	}

	public static ControlApiControlUpdateRequestDto toControlApiUpdateRequest(ControlUpdateRequestDto request) {
		return new ControlApiControlUpdateRequestDto(request.groupId(), request.parentControlId(),
				request.controlRef(), request.title(), request.statement());
	}

}
