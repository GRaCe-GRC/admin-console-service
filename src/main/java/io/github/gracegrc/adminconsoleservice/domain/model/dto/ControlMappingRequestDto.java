package io.github.gracegrc.adminconsoleservice.domain.model.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ControlMappingRequestDto(
		@NotNull UUID requirementId,
		@NotNull UUID controlId,
		String rationale) {
}
