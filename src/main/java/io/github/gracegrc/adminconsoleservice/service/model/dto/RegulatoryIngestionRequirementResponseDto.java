package io.github.gracegrc.adminconsoleservice.service.model.dto;

import java.time.Instant;
import java.util.UUID;

public record RegulatoryIngestionRequirementResponseDto(
		UUID id,
		UUID fileId,
		UUID parentRequirementId,
		String provisionRef,
		String heading,
		String text,
		Instant createdAt,
		String createdBy) {
}
