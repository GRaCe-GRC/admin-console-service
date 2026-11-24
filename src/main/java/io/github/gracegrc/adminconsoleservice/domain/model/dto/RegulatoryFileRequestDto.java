package io.github.gracegrc.adminconsoleservice.domain.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record RegulatoryFileRequestDto(
		@NotNull UUID frameworkId,
		@NotBlank @Size(max = 255) String title,
		String sourceUrl,
		@Size(max = 255) String versionLabel,
		LocalDate publicationDate) {
}
