package io.github.gracegrc.adminconsoleservice.domain.model.dto;

import java.time.Instant;
import java.util.UUID;

public record RegulatoryAuthorityResponseDto(
		UUID id,
		String name,
		String jurisdiction,
		Instant createdAt,
		boolean enabled,
		String createdBy) {
}
