package io.github.gracegrc.adminconsoleservice.domain.model.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DashboardAuthorityResponseDto(
		UUID id,
		String name,
		String jurisdiction,
		boolean enabled,
		Instant createdAt,
		String createdBy,
		List<DashboardFrameworkResponseDto> frameworks) {
}
