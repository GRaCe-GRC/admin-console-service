package io.github.gracegrc.adminconsoleservice.service.model.dto;

import java.time.LocalDate;

public record RegulatoryApiFileUpdateRequestDto(
		String title,
		String sourceUrl,
		String versionLabel,
		LocalDate publicationDate) {
}
