package io.github.gracegrc.adminconsoleservice.domain.model.dto;

import java.util.UUID;

public record DashboardMappedControlResponseDto(
		UUID mappingId,
		UUID controlId,
		String controlRef,
		String title,
		String rationale) {
}
