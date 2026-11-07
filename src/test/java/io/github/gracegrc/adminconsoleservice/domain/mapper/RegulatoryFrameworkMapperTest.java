package io.github.gracegrc.adminconsoleservice.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFrameworkRequestDto;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegulatoryFrameworkMapperTest {

	private static final UUID AUTHORITY_ID = UUID.fromString("3f2c1a9e-7d4b-4e8a-9c1f-2b6d5e8a7c40");

	@Test
	void toIngestionRequestCopiesTheFieldsAndAddsCreatedBy() {
		RegulatoryIngestionFrameworkRequestDto request = RegulatoryFrameworkMapper.toIngestionRequest(
				new RegulatoryFrameworkRequestDto(AUTHORITY_ID, "Digital Operational Resilience Act", "DORA",
						"Regulation (EU) 2022/2554"),
				"admin-console");

		assertThat(request).isEqualTo(new RegulatoryIngestionFrameworkRequestDto(AUTHORITY_ID,
				"Digital Operational Resilience Act", "DORA", "Regulation (EU) 2022/2554", "admin-console"));
	}

}
