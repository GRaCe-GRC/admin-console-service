package io.github.gracegrc.adminconsoleservice.service.model.dto;

import java.util.UUID;

public record ControlApiControlGroupRequestDto(
		UUID parentGroupId,
		String groupRef,
		String title,
		String createdBy) {
}
