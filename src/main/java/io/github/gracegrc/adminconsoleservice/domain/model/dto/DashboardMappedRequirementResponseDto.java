package io.github.gracegrc.adminconsoleservice.domain.model.dto;

import java.util.UUID;

public record DashboardMappedRequirementResponseDto(
		UUID mappingId,
		UUID requirementId,
		String provisionRef,
		String heading,
		UUID fileId,
		String fileTitle,
		UUID frameworkId,
		String frameworkName,
		UUID authorityId,
		String authorityName,
		String rationale) {
}
