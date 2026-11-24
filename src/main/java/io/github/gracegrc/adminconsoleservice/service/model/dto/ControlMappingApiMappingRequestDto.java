package io.github.gracegrc.adminconsoleservice.service.model.dto;

import java.util.UUID;

public record ControlMappingApiMappingRequestDto(
		UUID requirementId,
		UUID controlId,
		String rationale,
		String createdBy) {
}
