package io.github.gracegrc.adminconsoleservice.service.regulatory;

import static org.mockito.BDDMockito.then;

import io.github.gracegrc.adminconsoleservice.boot.client.RegulatoryApiClient;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFileUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFrameworkUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryApiAuthorityUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryApiFileUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryApiFrameworkUpdateRequestDto;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegulatoryApiServiceImplTest {

	private static final UUID FILE_ID = UUID.fromString("9b2c3d4e-5f60-4b7c-8d9e-1f2a3b4c5d6e");

	private static final UUID ID = UUID.fromString("3f2c1a9e-7d4b-4e8a-9c1f-2b6d5e8a7c40");

	@Mock
	private RegulatoryApiClient regulatoryApiClient;

	@InjectMocks
	private RegulatoryApiServiceImpl regulatoryApiService;

	@Test
	void updateAuthoritySendsEveryFieldToTheRegulatoryApi() {
		regulatoryApiService.updateAuthority(ID, new RegulatoryAuthorityUpdateRequestDto("EBA", "EU", false));

		then(regulatoryApiClient).should().updateAuthority(ID, new RegulatoryApiAuthorityUpdateRequestDto("EBA", "EU", false));
	}

	@Test
	void updateFrameworkSendsEveryFieldToTheRegulatoryApi() {
		regulatoryApiService.updateFramework(ID, new RegulatoryFrameworkUpdateRequestDto("DORA", null, null, true));

		then(regulatoryApiClient).should()
			.updateFramework(ID, new RegulatoryApiFrameworkUpdateRequestDto("DORA", null, null, true));
	}

	@Test
	void updateFileSendsEveryFieldToTheRegulatoryApi() {
		LocalDate publicationDate = LocalDate.parse("2022-12-27");

		regulatoryApiService.updateFile(FILE_ID,
				new RegulatoryFileUpdateRequestDto("DORA", "https://example.org/dora", "OJ L 333", publicationDate));

		then(regulatoryApiClient).should()
			.updateFile(FILE_ID, new RegulatoryApiFileUpdateRequestDto("DORA", "https://example.org/dora", "OJ L 333",
					publicationDate));
	}

}
