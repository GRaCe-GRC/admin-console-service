package io.github.gracegrc.adminconsoleservice.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionAuthorityResponseDto;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegulatoryAuthorityMapperTest {

	@Test
	void toIngestionRequestCopiesTheFieldsAndAddsCreatedBy() {
		RegulatoryIngestionAuthorityRequestDto request = RegulatoryAuthorityMapper
			.toIngestionRequest(new RegulatoryAuthorityRequestDto("EBA", "EU"), "admin-console");

		assertThat(request).isEqualTo(new RegulatoryIngestionAuthorityRequestDto("EBA", "EU", "admin-console"));
	}

	@Test
	void toResponseDtoCopiesEveryField() {
		UUID id = UUID.fromString("3f2c1a9e-7d4b-4e8a-9c1f-2b6d5e8a7c40");
		Instant createdAt = Instant.parse("2026-10-01T10:15:30Z");

		RegulatoryAuthorityResponseDto response = RegulatoryAuthorityMapper.toResponseDto(
				new RegulatoryIngestionAuthorityResponseDto(id, "EBA", "EU", createdAt, false, "admin-console"));

		assertThat(response).isEqualTo(new RegulatoryAuthorityResponseDto(id, "EBA", "EU", createdAt, false, "admin-console"));
	}

}
