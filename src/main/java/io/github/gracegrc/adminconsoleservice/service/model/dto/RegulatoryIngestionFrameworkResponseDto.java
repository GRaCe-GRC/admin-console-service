package io.github.gracegrc.adminconsoleservice.service.model.dto;

import java.time.Instant;
import java.util.UUID;

public record RegulatoryIngestionFrameworkResponseDto(
		UUID id,
		UUID authorityId,
		String name,
		String shortName,
		String citation,
		boolean enabled,
		Instant createdAt,
		String createdBy) {
}
