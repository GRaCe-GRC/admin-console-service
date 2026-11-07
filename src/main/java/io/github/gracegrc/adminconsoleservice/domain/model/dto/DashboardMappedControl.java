package io.github.gracegrc.adminconsoleservice.domain.model.dto;

import java.util.UUID;

public record DashboardMappedControl(
		UUID mappingId,
		UUID controlId,
		String rationale) {
}
