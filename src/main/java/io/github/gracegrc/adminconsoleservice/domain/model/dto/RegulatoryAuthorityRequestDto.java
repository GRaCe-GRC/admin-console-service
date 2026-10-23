package io.github.gracegrc.adminconsoleservice.domain.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegulatoryAuthorityRequestDto(
		@NotBlank @Size(max = 255) String name,
		@NotBlank @Size(max = 255) String jurisdiction) {
}
