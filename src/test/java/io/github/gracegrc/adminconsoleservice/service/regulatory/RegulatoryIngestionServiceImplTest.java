package io.github.gracegrc.adminconsoleservice.service.regulatory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.gracegrc.adminconsoleservice.boot.client.RegulatoryIngestionServiceClient;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionAuthorityResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFileResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFrameworkRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFrameworkResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionRequirementResponseDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegulatoryIngestionServiceImplTest {

	private static final UUID ID = UUID.fromString("3f2c1a9e-7d4b-4e8a-9c1f-2b6d5e8a7c40");

	private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");

	private static final RegulatoryIngestionAuthorityResponseDto INGESTION_AUTHORITY = new RegulatoryIngestionAuthorityResponseDto(
			ID, "EBA", "EU", CREATED_AT, true, "admin-console");

	private static final RegulatoryAuthorityResponseDto AUTHORITY = new RegulatoryAuthorityResponseDto(ID, "EBA", "EU",
			CREATED_AT, true, "admin-console");

	@Mock
	private RegulatoryIngestionServiceClient regulatoryIngestionServiceClient;

	@InjectMocks
	private RegulatoryIngestionServiceImpl regulatoryIngestionService;

	@Test
	void createAuthoritySendsTheRequestWithCreatedByAdminConsole() {
		given(regulatoryIngestionServiceClient
			.createAuthority(new RegulatoryIngestionAuthorityRequestDto("EBA", "EU", "admin-console")))
			.willReturn(ID);

		assertThat(regulatoryIngestionService.createAuthority(new RegulatoryAuthorityRequestDto("EBA", "EU")))
			.isEqualTo(ID);
	}

	@Test
	void findAllAuthoritiesMapsTheListFromTheClient() {
		given(regulatoryIngestionServiceClient.findAllAuthorities()).willReturn(List.of(INGESTION_AUTHORITY));

		assertThat(regulatoryIngestionService.findAllAuthorities()).containsExactly(AUTHORITY);
	}

	@Test
	void deleteAuthorityAsksTheClientToDeleteIt() {
		regulatoryIngestionService.deleteAuthority(ID);

		then(regulatoryIngestionServiceClient).should().deleteAuthority(ID);
	}

	@Test
	void createFrameworkSendsTheRequestWithCreatedByAdminConsole() {
		UUID frameworkId = UUID.fromString("8a1b2c3d-4e5f-4a6b-9c7d-0e1f2a3b4c5d");
		given(regulatoryIngestionServiceClient
			.createFramework(new RegulatoryIngestionFrameworkRequestDto(ID, "DORA", "DORA", null, "admin-console")))
			.willReturn(frameworkId);

		assertThat(regulatoryIngestionService.createFramework(new RegulatoryFrameworkRequestDto(ID, "DORA", "DORA", null)))
			.isEqualTo(frameworkId);
	}

	@Test
	void findAllFrameworksReturnsTheFrameworksFromTheClient() {
		List<RegulatoryIngestionFrameworkResponseDto> frameworks = List.of(new RegulatoryIngestionFrameworkResponseDto(
				UUID.randomUUID(), ID, "DORA", null, null, true, CREATED_AT, "admin-console"));
		given(regulatoryIngestionServiceClient.findAllFrameworks()).willReturn(frameworks);

		assertThat(regulatoryIngestionService.findAllFrameworks()).isEqualTo(frameworks);
	}

	@Test
	void deleteFrameworkAsksTheClientToDeleteIt() {
		regulatoryIngestionService.deleteFramework(ID);

		then(regulatoryIngestionServiceClient).should().deleteFramework(ID);
	}

	@Test
	void findAllFilesReturnsTheFilesFromTheClient() {
		List<RegulatoryIngestionFileResponseDto> files = List.of(new RegulatoryIngestionFileResponseDto(UUID.randomUUID(),
				ID, "DORA", null, null, null, CREATED_AT, "admin-console"));
		given(regulatoryIngestionServiceClient.findAllFiles()).willReturn(files);

		assertThat(regulatoryIngestionService.findAllFiles()).isEqualTo(files);
	}

	@Test
	void findAllRequirementsReturnsTheRequirementsFromTheClient() {
		List<RegulatoryIngestionRequirementResponseDto> requirements = List.of(new RegulatoryIngestionRequirementResponseDto(
				UUID.randomUUID(), ID, null, "Art. 5", null, null, CREATED_AT, "admin-console"));
		given(regulatoryIngestionServiceClient.findAllRequirements()).willReturn(requirements);

		assertThat(regulatoryIngestionService.findAllRequirements()).isEqualTo(requirements);
	}
}
