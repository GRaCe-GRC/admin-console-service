package io.github.gracegrc.adminconsoleservice.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import io.github.gracegrc.adminconsoleservice.domain.mapper.DashboardMapper;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardAuthority;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardRequirement;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFileResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFrameworkResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionRequirementResponseDto;
import io.github.gracegrc.adminconsoleservice.service.regulatory.RegulatoryIngestionService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

	private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");

	private static final RegulatoryAuthorityResponseDto EBA = authority("EBA");

	private static final RegulatoryAuthorityResponseDto BAFIN = authority("BaFin");

	private static final RegulatoryIngestionFrameworkResponseDto DORA = framework(EBA, "DORA");

	private static final RegulatoryIngestionFrameworkResponseDto MICA = framework(EBA, "MiCA");

	private static final RegulatoryIngestionFileResponseDto DORA_TEXT = file(DORA, "DORA text", "2022-12-27");

	private static final RegulatoryIngestionFileResponseDto DORA_RTS = file(DORA, "RTS", "2024-01-17");

	private static final RegulatoryIngestionFileResponseDto DORA_ANNEX = file(DORA, "Annex", null);

	private static final RegulatoryIngestionRequirementResponseDto ART_5 = requirement(DORA_TEXT, null, "Art. 5");

	private static final RegulatoryIngestionRequirementResponseDto ART_5_1 = requirement(DORA_TEXT, ART_5, "Art. 5(1)");

	private static final RegulatoryIngestionRequirementResponseDto ART_5_1_A = requirement(DORA_TEXT, ART_5_1,
			"Art. 5(1)(a)");

	private static final RegulatoryIngestionRequirementResponseDto ART_5_1_B = requirement(DORA_TEXT, ART_5_1,
			"Art. 5(1)(b)");

	private static final RegulatoryIngestionRequirementResponseDto ART_6 = requirement(DORA_TEXT, null, "Art. 6");

	private static final RegulatoryIngestionRequirementResponseDto RTS_1 = requirement(DORA_RTS, null, "Art. 1");

	@Mock
	private RegulatoryIngestionService regulatoryIngestionService;

	private ExecutorService executor;

	private DashboardServiceImpl dashboardService;

	@BeforeEach
	void createService() {
		executor = Executors.newVirtualThreadPerTaskExecutor();
		dashboardService = new DashboardServiceImpl(regulatoryIngestionService, executor);
	}

	@AfterEach
	void closeExecutor() {
		executor.close();
	}

	@Test
	void retrieveDashboardIssuesEveryCallThroughTheExecutor() {
		Set<Thread> threads = ConcurrentHashMap.newKeySet();
		given(regulatoryIngestionService.findAllAuthorities()).willAnswer(invocation -> record(threads));
		given(regulatoryIngestionService.findAllFrameworks()).willAnswer(invocation -> record(threads));
		given(regulatoryIngestionService.findAllFiles()).willAnswer(invocation -> record(threads));
		given(regulatoryIngestionService.findAllRequirements()).willAnswer(invocation -> record(threads));

		dashboardService.retrieveDashboard();

		assertThat(threads).hasSize(4).allSatisfy(thread -> assertThat(thread.isVirtual()).isTrue())
			.doesNotContain(Thread.currentThread());
	}

	@Test
	void retrieveDashboardThrowsAClientErrorAsTheCauseOfTheCompletionException() {
		HttpClientErrorException notFound = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null,
				new byte[0], StandardCharsets.UTF_8);
		given(regulatoryIngestionService.findAllFiles()).willThrow(notFound);

		assertThatThrownBy(() -> dashboardService.retrieveDashboard()).isInstanceOf(CompletionException.class)
			.cause()
			.isSameAs(notFound);
	}

	@Test
	void retrieveDashboardThrowsAnUnavailableServiceAsTheCauseOfTheCompletionException() {
		RestClientException unavailable = new RestClientException("The regulatory ingestion service is unavailable");
		given(regulatoryIngestionService.findAllRequirements()).willThrow(unavailable);

		assertThatThrownBy(() -> dashboardService.retrieveDashboard()).isInstanceOf(CompletionException.class)
			.cause()
			.isSameAs(unavailable);
	}

	@Test
	void retrieveDashboardReturnsTheFetchedListsPutTogetherByTheMapper() {
		givenTheExample();

		DashboardResponseDto dashboard = dashboardService.retrieveDashboard();

		assertThat(dashboard).isEqualTo(DashboardMapper.toResponseDto(List.of(EBA, BAFIN), List.of(MICA, DORA),
				List.of(DORA_ANNEX, DORA_RTS, DORA_TEXT), List.of(ART_6, ART_5_1_B, RTS_1, ART_5_1_A, ART_5_1, ART_5)));
		assertThat(dashboard.authorities()).extracting(DashboardAuthority::name).containsExactly("BaFin", "EBA");
		assertThat(dashboard.authorities().get(1).frameworks().getFirst().files().getFirst().requirements())
			.extracting(DashboardRequirement::provisionRef)
			.containsExactly("Art. 5", "Art. 6");
	}

	@Test
	void retrieveDashboardTreatsANullUpstreamListAsEmpty() {
		givenUpstream(List.of(EBA), List.of(DORA), null, null);

		assertThat(dashboardService.retrieveDashboard().authorities().getFirst().frameworks().getFirst().files())
			.isEmpty();
	}

	private void givenTheExample() {
		givenUpstream(List.of(EBA, BAFIN), List.of(MICA, DORA), List.of(DORA_ANNEX, DORA_RTS, DORA_TEXT),
				List.of(ART_6, ART_5_1_B, RTS_1, ART_5_1_A, ART_5_1, ART_5));
	}

	private void givenUpstream(List<RegulatoryAuthorityResponseDto> authorities,
			List<RegulatoryIngestionFrameworkResponseDto> frameworks, List<RegulatoryIngestionFileResponseDto> files,
			List<RegulatoryIngestionRequirementResponseDto> requirements) {
		given(regulatoryIngestionService.findAllAuthorities()).willReturn(authorities);
		given(regulatoryIngestionService.findAllFrameworks()).willReturn(frameworks);
		given(regulatoryIngestionService.findAllFiles()).willReturn(files);
		given(regulatoryIngestionService.findAllRequirements()).willReturn(requirements);
	}

	private static List<Object> record(Set<Thread> threads) {
		threads.add(Thread.currentThread());
		return List.of();
	}

	private static RegulatoryAuthorityResponseDto authority(String name) {
		return new RegulatoryAuthorityResponseDto(UUID.randomUUID(), name, "EU", CREATED_AT, true, "admin-console");
	}

	private static RegulatoryIngestionFrameworkResponseDto framework(RegulatoryAuthorityResponseDto authority,
			String name) {
		return new RegulatoryIngestionFrameworkResponseDto(UUID.randomUUID(), authority.id(), name, name + " short",
				name + " citation", true, CREATED_AT, "admin-console");
	}

	private static RegulatoryIngestionFileResponseDto file(RegulatoryIngestionFrameworkResponseDto framework,
			String title, String publicationDate) {
		return new RegulatoryIngestionFileResponseDto(UUID.randomUUID(), framework.id(), title,
				"https://example.org/" + title, "v1", publicationDate == null ? null : LocalDate.parse(publicationDate),
				CREATED_AT, "admin-console");
	}

	private static RegulatoryIngestionRequirementResponseDto requirement(RegulatoryIngestionFileResponseDto file,
			RegulatoryIngestionRequirementResponseDto parent, String provisionRef) {
		return new RegulatoryIngestionRequirementResponseDto(UUID.randomUUID(), file.id(),
				parent == null ? null : parent.id(), provisionRef, provisionRef + " heading", provisionRef + " text",
				CREATED_AT, "admin-console");
	}

}
