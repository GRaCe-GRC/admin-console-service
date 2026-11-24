package io.github.gracegrc.adminconsoleservice.domain.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ControlRequestDto(
		UUID groupId,
		UUID parentControlId,
		@NotBlank @Size(max = 255) String controlRef,
		@NotBlank @Size(max = 255) String title,
		String statement) {
}
