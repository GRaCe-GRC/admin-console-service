package io.github.gracegrc.adminconsoleservice.service.model.dto;

import java.util.UUID;

public record ControlApiControlGroupUpdateRequestDto(
		UUID parentGroupId,
		String groupRef,
		String title) {
}
