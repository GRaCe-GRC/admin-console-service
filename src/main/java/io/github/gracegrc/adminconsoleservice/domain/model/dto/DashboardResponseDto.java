package io.github.gracegrc.adminconsoleservice.domain.model.dto;

import java.util.List;

public record DashboardResponseDto(
		List<DashboardAuthority> authorities) {
}
