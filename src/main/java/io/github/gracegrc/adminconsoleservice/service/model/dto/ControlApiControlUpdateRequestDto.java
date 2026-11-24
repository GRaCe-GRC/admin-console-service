package io.github.gracegrc.adminconsoleservice.service.model.dto;

import java.util.UUID;

public record ControlApiControlUpdateRequestDto(
		UUID groupId,
		UUID parentControlId,
		String controlRef,
		String title,
		String statement) {
}
