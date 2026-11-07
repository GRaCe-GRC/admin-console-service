package io.github.gracegrc.adminconsoleservice.domain.model.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DashboardFramework(
		UUID id,
		String name,
		String shortName,
		String citation,
		boolean enabled,
		Instant createdAt,
		String createdBy,
		List<DashboardFile> files) {
}
