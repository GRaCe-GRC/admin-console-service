package io.github.gracegrc.adminconsoleservice.service.regulatory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.gracegrc.adminconsoleservice.boot.client.RegulatoryIngestionServiceClient;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionAuthorityResponseDto;
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
	void findAuthorityByIdMapsTheAuthorityFromTheClient() {
		given(regulatoryIngestionServiceClient.findAuthorityById(ID)).willReturn(INGESTION_AUTHORITY);

		assertThat(regulatoryIngestionService.findAuthorityById(ID)).isEqualTo(AUTHORITY);
	}

	@Test
	void deleteAuthorityAsksTheClientToDeleteIt() {
		regulatoryIngestionService.deleteAuthority(ID);

		then(regulatoryIngestionServiceClient).should().deleteAuthority(ID);
	}
}
