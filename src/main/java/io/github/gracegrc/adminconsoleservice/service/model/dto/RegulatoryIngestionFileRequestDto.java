package io.github.gracegrc.adminconsoleservice.service.model.dto;

import java.time.LocalDate;
import java.util.UUID;

public record RegulatoryIngestionFileRequestDto(
		UUID frameworkId,
		String title,
		String sourceUrl,
		String versionLabel,
		LocalDate publicationDate,
		String createdBy) {
}
