package io.github.gracegrc.adminconsoleservice.service.model.dto;

public record RegulatoryApiAuthorityUpdateRequestDto(
		String name,
		String jurisdiction,
		Boolean enabled) {
}
