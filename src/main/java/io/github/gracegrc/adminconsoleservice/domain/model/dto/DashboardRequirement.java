package io.github.gracegrc.adminconsoleservice.domain.model.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DashboardRequirement(
		UUID id,
		String provisionRef,
		String heading,
		String text,
		Instant createdAt,
		String createdBy,
		List<DashboardMappedControl> mappedControls,
		List<DashboardRequirement> children) {
}
