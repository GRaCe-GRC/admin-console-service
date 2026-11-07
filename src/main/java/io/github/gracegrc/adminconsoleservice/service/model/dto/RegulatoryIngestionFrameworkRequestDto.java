package io.github.gracegrc.adminconsoleservice.service.model.dto;

import java.util.UUID;

public record RegulatoryIngestionFrameworkRequestDto(
		UUID authorityId,
		String name,
		String shortName,
		String citation,
		String createdBy) {
}
