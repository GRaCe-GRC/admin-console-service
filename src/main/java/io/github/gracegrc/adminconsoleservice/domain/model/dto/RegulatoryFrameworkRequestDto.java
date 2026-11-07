package io.github.gracegrc.adminconsoleservice.domain.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record RegulatoryFrameworkRequestDto(
		@NotNull UUID authorityId,
		@NotBlank @Size(max = 255) String name,
		@Size(max = 255) String shortName,
		@Size(max = 255) String citation) {
}
