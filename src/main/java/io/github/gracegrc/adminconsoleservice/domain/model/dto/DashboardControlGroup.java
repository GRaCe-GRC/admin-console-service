package io.github.gracegrc.adminconsoleservice.domain.model.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DashboardControlGroup(
		UUID id,
		String groupRef,
		String title,
		Instant createdAt,
		String createdBy,
		List<DashboardControl> controls,
		List<DashboardControlGroup> children) {
}
