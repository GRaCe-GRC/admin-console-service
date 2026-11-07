package io.github.gracegrc.adminconsoleservice.service.model.dto;

import java.time.Instant;
import java.util.UUID;

public record ControlApiControlResponseDto(
		UUID id,
		UUID groupId,
		UUID parentControlId,
		String controlRef,
		String title,
		String statement,
		Instant createdAt,
		String createdBy) {
}
