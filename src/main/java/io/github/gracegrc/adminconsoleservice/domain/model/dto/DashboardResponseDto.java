package io.github.gracegrc.adminconsoleservice.domain.model.dto;

import java.util.List;

public record DashboardResponseDto(
		List<DashboardAuthorityResponseDto> authorities,
		List<DashboardControlGroupResponseDto> controlGroups,
		List<DashboardControlResponseDto> ungroupedControls) {
}
