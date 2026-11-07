package io.github.gracegrc.adminconsoleservice.service.model.dto;

import java.time.Instant;
import java.util.UUID;

public record ControlMappingApiMappingResponseDto(
		UUID id,
		UUID requirementId,
		UUID controlId,
		String rationale,
		Instant createdAt,
		String createdBy) {
}
