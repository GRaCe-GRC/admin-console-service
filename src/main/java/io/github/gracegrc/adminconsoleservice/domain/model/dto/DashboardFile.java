package io.github.gracegrc.adminconsoleservice.domain.model.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DashboardFile(
		UUID id,
		String title,
		String sourceUrl,
		String versionLabel,
		LocalDate publicationDate,
		Instant createdAt,
		String createdBy,
		DashboardCoverage coverage,
		List<DashboardRequirement> requirements) {
}
