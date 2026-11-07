package io.github.gracegrc.adminconsoleservice.service.control;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import io.github.gracegrc.adminconsoleservice.boot.client.ControlMappingApiClient;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingResponseDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ControlMappingApiServiceImplTest {

	@Mock
	private ControlMappingApiClient controlMappingApiClient;

	@InjectMocks
	private ControlMappingApiServiceImpl controlMappingApiService;

	@Test
	void findAllMappingsReturnsTheMappingsFromTheClient() {
		UUID requirementId = UUID.fromString("5d6e7f80-1a2b-4c3d-8e4f-5a6b7c8d9e0f");
		List<ControlMappingApiMappingResponseDto> mappings = List.of(new ControlMappingApiMappingResponseDto(
				UUID.randomUUID(), requirementId, UUID.randomUUID(), null, Instant.parse("2026-10-01T10:15:30Z"), "me"));
		given(controlMappingApiClient.findAllMappings()).willReturn(mappings);

		assertThat(controlMappingApiService.findAllMappings()).isEqualTo(mappings);
	}

}
