package io.github.gracegrc.adminconsoleservice.domain.model.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DashboardControlResponseDto(
		UUID id,
		String controlRef,
		String title,
		String statement,
		Instant createdAt,
		String createdBy,
		List<DashboardMappedRequirementResponseDto> mappedRequirements,
		List<DashboardControlResponseDto> enhancements) {
}
