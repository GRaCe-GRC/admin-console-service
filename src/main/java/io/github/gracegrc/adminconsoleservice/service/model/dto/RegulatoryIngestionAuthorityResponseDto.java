package io.github.gracegrc.adminconsoleservice.service.model.dto;

import java.time.Instant;
import java.util.UUID;

public record RegulatoryIngestionAuthorityResponseDto(
		UUID id,
		String name,
		String jurisdiction,
		Instant createdAt,
		boolean enabled,
		String createdBy) {
}
