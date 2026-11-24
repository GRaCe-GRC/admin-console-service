package io.github.gracegrc.adminconsoleservice.domain.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ControlGroupRequestDto(
		UUID parentGroupId,
		@NotBlank @Size(max = 255) String groupRef,
		@NotBlank @Size(max = 255) String title) {
}
