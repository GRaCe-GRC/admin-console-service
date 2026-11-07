package io.github.gracegrc.adminconsoleservice.domain.model.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DashboardControlGroupResponseDto(
		UUID id,
		String groupRef,
		String title,
		Instant createdAt,
		String createdBy,
		List<DashboardControlResponseDto> controls,
		List<DashboardControlGroupResponseDto> children) {
}
