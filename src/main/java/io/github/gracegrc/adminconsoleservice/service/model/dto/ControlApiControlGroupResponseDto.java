package io.github.gracegrc.adminconsoleservice.service.model.dto;

import java.time.Instant;
import java.util.UUID;

public record ControlApiControlGroupResponseDto(
		UUID id,
		UUID parentGroupId,
		String groupRef,
		String title,
		Instant createdAt,
		String createdBy) {
}
