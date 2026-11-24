package io.github.gracegrc.adminconsoleservice.service.model.dto;

import java.util.UUID;

public record ControlApiControlRequestDto(
		UUID groupId,
		UUID parentControlId,
		String controlRef,
		String title,
		String statement,
		String createdBy) {
}
