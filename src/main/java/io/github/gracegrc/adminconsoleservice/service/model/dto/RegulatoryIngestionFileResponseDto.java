package io.github.gracegrc.adminconsoleservice.service.model.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RegulatoryIngestionFileResponseDto(
		UUID id,
		UUID frameworkId,
		String title,
		String sourceUrl,
		String versionLabel,
		LocalDate publicationDate,
		Instant createdAt,
		String createdBy) {
}
