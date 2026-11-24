package io.github.gracegrc.adminconsoleservice.service.model.dto;

public record RegulatoryApiFrameworkUpdateRequestDto(
		String name,
		String shortName,
		String citation,
		Boolean enabled) {
}
