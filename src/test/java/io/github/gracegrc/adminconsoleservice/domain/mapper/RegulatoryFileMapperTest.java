package io.github.gracegrc.adminconsoleservice.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFileRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFileUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryApiFileUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFileRequestDto;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegulatoryFileMapperTest {

	private static final UUID FRAMEWORK_ID = UUID.fromString("8a1b2c3d-4e5f-4a6b-9c7d-0e1f2a3b4c5d");

	private static final LocalDate PUBLICATION_DATE = LocalDate.parse("2022-12-27");

	@Test
	void toIngestionRequestCopiesTheFieldsAndAddsCreatedBy() {
		RegulatoryIngestionFileRequestDto request = RegulatoryFileMapper.toIngestionRequest(
				new RegulatoryFileRequestDto(FRAMEWORK_ID, "DORA", "https://example.org/dora", "OJ L 333",
						PUBLICATION_DATE),
				"admin-console");

		assertThat(request).isEqualTo(new RegulatoryIngestionFileRequestDto(FRAMEWORK_ID, "DORA",
				"https://example.org/dora", "OJ L 333", PUBLICATION_DATE, "admin-console"));
	}

	@Test
	void toRegulatoryApiUpdateRequestCopiesEveryField() {
		RegulatoryApiFileUpdateRequestDto request = RegulatoryFileMapper.toRegulatoryApiUpdateRequest(
				new RegulatoryFileUpdateRequestDto("DORA", "https://example.org/dora", "OJ L 333", PUBLICATION_DATE));

		assertThat(request).isEqualTo(
				new RegulatoryApiFileUpdateRequestDto("DORA", "https://example.org/dora", "OJ L 333", PUBLICATION_DATE));
	}

}
