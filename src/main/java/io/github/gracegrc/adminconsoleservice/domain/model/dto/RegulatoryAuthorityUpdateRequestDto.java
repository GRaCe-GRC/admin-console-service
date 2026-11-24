package io.github.gracegrc.adminconsoleservice.domain.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegulatoryAuthorityUpdateRequestDto(
		@NotBlank @Size(max = 255) String name,
		@NotBlank @Size(max = 255) String jurisdiction,
		@NotNull Boolean enabled) {
}
