package io.github.gracegrc.adminconsoleservice.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardAuthorityResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardControlGroupResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardControlResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardCoverageResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardFileResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardFrameworkResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardMappedControlResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardMappedRequirementResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardRequirementResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.adminconsoleservice.service.control.ControlApiService;
import io.github.gracegrc.adminconsoleservice.service.control.ControlMappingApiService;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingResponseDto;
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

	private static final ControlApiControlGroupResponseDto AC = group(null, "AC");

	private static final ControlApiControlGroupResponseDto AC_1 = group(AC, "AC.1");

	private static final ControlApiControlGroupResponseDto AU = group(null, "AU");

	private static final ControlApiControlResponseDto AC_2 = control(AC, null, "AC-2");

	private static final ControlApiControlResponseDto AC_2_1 = control(AC, AC_2, "AC-2(1)");

	private static final ControlApiControlResponseDto AC_3 = control(AC_1, null, "AC-3");

	private static final ControlApiControlResponseDto PM_1 = control(null, null, "PM-1");

	private static final ControlApiControlResponseDto PM_1_1 = control(null, PM_1, "PM-1(1)");

	private static final ControlMappingApiMappingResponseDto ART_5_1_A_TO_AC_2 = mapping(ART_5_1_A.id(), AC_2.id());

	private static final ControlMappingApiMappingResponseDto ART_5_1_A_TO_AC_3 = mapping(ART_5_1_A.id(), AC_3.id());

	private static final ControlMappingApiMappingResponseDto ART_5_TO_AC_2 = mapping(ART_5.id(), AC_2.id());

	private static final ControlMappingApiMappingResponseDto RTS_1_TO_AC_2_1 = mapping(RTS_1.id(), AC_2_1.id());

	private static final ControlMappingApiMappingResponseDto UNKNOWN_REQUIREMENT_TO_AC_2 = mapping(UUID.randomUUID(),
			AC_2.id());

	private static final ControlMappingApiMappingResponseDto ART_6_TO_UNKNOWN_CONTROL = mapping(ART_6.id(),
			UUID.randomUUID());

	@Mock
	private RegulatoryIngestionService regulatoryIngestionService;

	@Mock
	private ControlApiService controlApiService;

	@Mock
	private ControlMappingApiService controlMappingApiService;

	private ExecutorService executor;

	private DashboardServiceImpl dashboardService;

	@BeforeEach
	void createService() {
		executor = Executors.newVirtualThreadPerTaskExecutor();
		dashboardService = new DashboardServiceImpl(regulatoryIngestionService, controlApiService,
				controlMappingApiService, executor);
	}

	@AfterEach
	void closeExecutor() {
		executor.close();
	}

	@Test
	void findDashboardNestsRequirementsByTheirParent() {
		givenTheExample();

		DashboardFileResponseDto doraText = doraFile(dashboardService.findDashboard(), DORA_TEXT);

		assertThat(doraText.requirements()).extracting(DashboardRequirementResponseDto::provisionRef)
			.containsExactly("Art. 5", "Art. 6");
		DashboardRequirementResponseDto art5 = doraText.requirements().getFirst();
		assertThat(art5.children()).extracting(DashboardRequirementResponseDto::provisionRef).containsExactly("Art. 5(1)");
		assertThat(art5.children().getFirst().children()).extracting(DashboardRequirementResponseDto::provisionRef)
			.containsExactly("Art. 5(1)(a)", "Art. 5(1)(b)");
		assertThat(art5.children().getFirst().children()).allSatisfy(leaf -> assertThat(leaf.children()).isEmpty());
	}

	@Test
	void findDashboardCountsTheMappedLowestLevelProvisionsPerFileAndFramework() {
		givenTheExample();

		DashboardFrameworkResponseDto dora = framework(dashboardService.findDashboard(), DORA);

		assertThat(dora.files()).extracting(DashboardFileResponseDto::id, DashboardFileResponseDto::coverage)
			.containsExactly(tuple(DORA_TEXT.id(), new DashboardCoverageResponseDto(1, 3)),
					tuple(DORA_RTS.id(), new DashboardCoverageResponseDto(1, 1)),
					tuple(DORA_ANNEX.id(), new DashboardCoverageResponseDto(0, 0)));
		assertThat(dora.coverage()).isEqualTo(new DashboardCoverageResponseDto(2, 4));
	}

	@Test
	void findDashboardListsTheMappedControlsOfEveryRequirement() {
		givenTheExample();

		DashboardFileResponseDto doraText = doraFile(dashboardService.findDashboard(), DORA_TEXT);

		DashboardRequirementResponseDto art5 = doraText.requirements().getFirst();
		assertThat(art5.mappedControls()).containsExactly(new DashboardMappedControlResponseDto(ART_5_TO_AC_2.id(),
				AC_2.id(), "AC-2", "AC-2 title", "Because"));
		assertThat(art5.children().getFirst().children().getFirst().mappedControls())
			.extracting(DashboardMappedControlResponseDto::mappingId)
			.containsExactly(ART_5_1_A_TO_AC_2.id(), ART_5_1_A_TO_AC_3.id());
		assertThat(doraText.requirements().get(1).mappedControls()).isEmpty();
	}

	@Test
	void findDashboardNestsGroupsControlsAndEnhancements() {
		givenTheExample();

		DashboardResponseDto dashboard = dashboardService.findDashboard();

		assertThat(dashboard.controlGroups()).extracting(DashboardControlGroupResponseDto::groupRef)
			.containsExactly("AC", "AU");
		DashboardControlGroupResponseDto ac = dashboard.controlGroups().getFirst();
		assertThat(ac.controls()).extracting(DashboardControlResponseDto::controlRef).containsExactly("AC-2");
		assertThat(ac.controls().getFirst().enhancements()).extracting(DashboardControlResponseDto::controlRef)
			.containsExactly("AC-2(1)");
		assertThat(ac.children()).extracting(DashboardControlGroupResponseDto::groupRef).containsExactly("AC.1");
		assertThat(ac.children().getFirst().controls()).extracting(DashboardControlResponseDto::controlRef)
			.containsExactly("AC-3");
		assertThat(dashboard.controlGroups().get(1).controls()).isEmpty();
		assertThat(dashboard.ungroupedControls()).extracting(DashboardControlResponseDto::controlRef)
			.containsExactly("PM-1");
		assertThat(dashboard.ungroupedControls().getFirst().enhancements())
			.extracting(DashboardControlResponseDto::controlRef)
			.containsExactly("PM-1(1)");
	}

	@Test
	void findDashboardListsTheMappedRequirementsOfEveryControlWithoutOrphans() {
		givenTheExample();

		DashboardControlResponseDto ac2 = dashboardService.findDashboard().controlGroups().getFirst().controls().getFirst();

		assertThat(ac2.mappedRequirements()).containsExactly(
				new DashboardMappedRequirementResponseDto(ART_5_TO_AC_2.id(), ART_5.id(), "Art. 5", "Art. 5 heading",
						DORA_TEXT.id(), "DORA text", DORA.id(), "DORA", EBA.id(), "EBA", "Because"),
				new DashboardMappedRequirementResponseDto(ART_5_1_A_TO_AC_2.id(), ART_5_1_A.id(), "Art. 5(1)(a)",
						"Art. 5(1)(a) heading", DORA_TEXT.id(), "DORA text", DORA.id(), "DORA", EBA.id(), "EBA",
						"Because"));
		assertThat(ac2.enhancements().getFirst().mappedRequirements())
			.extracting(DashboardMappedRequirementResponseDto::mappingId)
			.containsExactly(RTS_1_TO_AC_2_1.id());
	}

	@Test
	void findDashboardSortsAuthoritiesFrameworksAndFiles() {
		givenTheExample();

		DashboardResponseDto dashboard = dashboardService.findDashboard();

		assertThat(dashboard.authorities()).extracting(DashboardAuthorityResponseDto::name).containsExactly("BaFin", "EBA");
		assertThat(dashboard.authorities().getFirst().frameworks()).isEmpty();
		DashboardAuthorityResponseDto eba = dashboard.authorities().get(1);
		assertThat(eba.frameworks()).extracting(DashboardFrameworkResponseDto::name).containsExactly("DORA", "MiCA");
		assertThat(eba.frameworks().get(1).files()).isEmpty();
		assertThat(eba.frameworks().get(1).coverage()).isEqualTo(new DashboardCoverageResponseDto(0, 0));
		assertThat(eba.frameworks().getFirst().files()).extracting(DashboardFileResponseDto::title)
			.containsExactly("DORA text", "RTS", "Annex");
	}

	@Test
	void findDashboardCopiesTheUpstreamFields() {
		givenTheExample();

		DashboardResponseDto dashboard = dashboardService.findDashboard();

		assertThat(dashboard.authorities().get(1)).usingRecursiveComparison()
			.comparingOnlyFields("id", "name", "jurisdiction", "enabled", "createdAt", "createdBy")
			.isEqualTo(EBA);
		assertThat(framework(dashboard, DORA)).usingRecursiveComparison()
			.comparingOnlyFields("id", "name", "shortName", "citation", "enabled", "createdAt", "createdBy")
			.isEqualTo(DORA);
		assertThat(doraFile(dashboard, DORA_TEXT)).usingRecursiveComparison()
			.comparingOnlyFields("id", "title", "sourceUrl", "versionLabel", "publicationDate", "createdAt", "createdBy")
			.isEqualTo(DORA_TEXT);
		assertThat(doraFile(dashboard, DORA_TEXT).requirements().getFirst()).usingRecursiveComparison()
			.comparingOnlyFields("id", "provisionRef", "heading", "text", "createdAt", "createdBy")
			.isEqualTo(ART_5);
		assertThat(dashboard.controlGroups().getFirst()).usingRecursiveComparison()
			.comparingOnlyFields("id", "groupRef", "title", "createdAt", "createdBy")
			.isEqualTo(AC);
		assertThat(dashboard.controlGroups().getFirst().controls().getFirst()).usingRecursiveComparison()
			.comparingOnlyFields("id", "controlRef", "title", "statement", "createdAt", "createdBy")
			.isEqualTo(AC_2);
	}

	@Test
	void findDashboardLeavesOutMappingsOfRequirementsWhoseFileFrameworkOrAuthorityIsMissing() {
		RegulatoryIngestionFrameworkResponseDto withoutAuthority = framework(BAFIN, "PSD3");
		RegulatoryIngestionFileResponseDto withoutFramework = file(MICA, "MiCA text", null);
		RegulatoryIngestionFileResponseDto inFrameworkWithoutAuthority = file(withoutAuthority, "PSD3 text", null);
		RegulatoryIngestionRequirementResponseDto withoutFile = requirement(DORA_TEXT, null, "Art. 1");
		RegulatoryIngestionRequirementResponseDto inFileWithoutFramework = requirement(withoutFramework, null, "Art. 2");
		RegulatoryIngestionRequirementResponseDto inFrameworkWithoutAuthorityRequirement = requirement(
				inFrameworkWithoutAuthority, null, "Art. 3");
		givenUpstream(List.of(EBA), List.of(withoutAuthority), List.of(withoutFramework, inFrameworkWithoutAuthority),
				List.of(withoutFile, inFileWithoutFramework, inFrameworkWithoutAuthorityRequirement), List.of(),
				List.of(PM_1), List.of(mapping(withoutFile.id(), PM_1.id()), mapping(inFileWithoutFramework.id(), PM_1.id()),
						mapping(inFrameworkWithoutAuthorityRequirement.id(), PM_1.id())));

		DashboardResponseDto dashboard = dashboardService.findDashboard();

		assertThat(dashboard.authorities()).extracting(DashboardAuthorityResponseDto::frameworks)
			.containsExactly(List.of());
		assertThat(dashboard.ungroupedControls().getFirst().mappedRequirements()).isEmpty();
	}

	@Test
	void findDashboardWithEmptyUpstreamListsReturnsEmptyArrays() {
		givenUpstream(List.of(), List.of(), List.of(), List.of(), List.of(), null, List.of());

		assertThat(dashboardService.findDashboard())
			.isEqualTo(new DashboardResponseDto(List.of(), List.of(), List.of()));
	}

	@Test
	void findDashboardIssuesEverySevenCallsThroughTheExecutor() {
		Set<Thread> threads = ConcurrentHashMap.newKeySet();
		given(regulatoryIngestionService.findAllAuthorities()).willAnswer(invocation -> record(threads));
		given(regulatoryIngestionService.findAllFrameworks()).willAnswer(invocation -> record(threads));
		given(regulatoryIngestionService.findAllFiles()).willAnswer(invocation -> record(threads));
		given(regulatoryIngestionService.findAllRequirements()).willAnswer(invocation -> record(threads));
		given(controlApiService.findAllControlGroups()).willAnswer(invocation -> record(threads));
		given(controlApiService.findAllControls()).willAnswer(invocation -> record(threads));
		given(controlMappingApiService.findAllMappings()).willAnswer(invocation -> record(threads));

		dashboardService.findDashboard();

		assertThat(threads).hasSize(7).allSatisfy(thread -> assertThat(thread.isVirtual()).isTrue())
			.doesNotContain(Thread.currentThread());
	}

	@Test
	void findDashboardPassesAClientErrorThroughUnwrapped() {
		HttpClientErrorException notFound = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null,
				new byte[0], StandardCharsets.UTF_8);
		given(controlApiService.findAllControls()).willThrow(notFound);

		assertThatThrownBy(() -> dashboardService.findDashboard()).isSameAs(notFound);
	}

	@Test
	void findDashboardPassesAnUnavailableApiThroughUnwrapped() {
		RestClientException unavailable = new RestClientException("The control mapping API is unavailable");
		given(controlMappingApiService.findAllMappings()).willThrow(unavailable);

		assertThatThrownBy(() -> dashboardService.findDashboard()).isSameAs(unavailable);
	}

	private void givenTheExample() {
		givenUpstream(List.of(EBA, BAFIN), List.of(MICA, DORA), List.of(DORA_ANNEX, DORA_RTS, DORA_TEXT),
				List.of(ART_6, ART_5_1_B, RTS_1, ART_5_1_A, ART_5_1, ART_5), List.of(AU, AC_1, AC),
				List.of(PM_1_1, AC_3, PM_1, AC_2_1, AC_2),
				List.of(UNKNOWN_REQUIREMENT_TO_AC_2, ART_5_1_A_TO_AC_3, ART_6_TO_UNKNOWN_CONTROL, RTS_1_TO_AC_2_1,
						ART_5_TO_AC_2, ART_5_1_A_TO_AC_2));
	}

	private void givenUpstream(List<RegulatoryAuthorityResponseDto> authorities,
			List<RegulatoryIngestionFrameworkResponseDto> frameworks, List<RegulatoryIngestionFileResponseDto> files,
			List<RegulatoryIngestionRequirementResponseDto> requirements, List<ControlApiControlGroupResponseDto> groups,
			List<ControlApiControlResponseDto> controls, List<ControlMappingApiMappingResponseDto> mappings) {
		given(regulatoryIngestionService.findAllAuthorities()).willReturn(authorities);
		given(regulatoryIngestionService.findAllFrameworks()).willReturn(frameworks);
		given(regulatoryIngestionService.findAllFiles()).willReturn(files);
		given(regulatoryIngestionService.findAllRequirements()).willReturn(requirements);
		given(controlApiService.findAllControlGroups()).willReturn(groups);
		given(controlApiService.findAllControls()).willReturn(controls);
		given(controlMappingApiService.findAllMappings()).willReturn(mappings);
	}

	private static List<Object> record(Set<Thread> threads) {
		threads.add(Thread.currentThread());
		return List.of();
	}

	private static DashboardFrameworkResponseDto framework(DashboardResponseDto dashboard,
			RegulatoryIngestionFrameworkResponseDto framework) {
		return dashboard.authorities()
			.stream()
			.flatMap(authority -> authority.frameworks().stream())
			.filter(candidate -> candidate.id().equals(framework.id()))
			.findFirst()
			.orElseThrow();
	}

	private static DashboardFileResponseDto doraFile(DashboardResponseDto dashboard,
			RegulatoryIngestionFileResponseDto file) {
		return framework(dashboard, DORA).files()
			.stream()
			.filter(candidate -> candidate.id().equals(file.id()))
			.findFirst()
			.orElseThrow();
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

	private static ControlApiControlGroupResponseDto group(ControlApiControlGroupResponseDto parent, String groupRef) {
		return new ControlApiControlGroupResponseDto(UUID.randomUUID(), parent == null ? null : parent.id(), groupRef,
				groupRef + " title", CREATED_AT, "me");
	}

	private static ControlApiControlResponseDto control(ControlApiControlGroupResponseDto group,
			ControlApiControlResponseDto parent, String controlRef) {
		return new ControlApiControlResponseDto(UUID.randomUUID(), group == null ? null : group.id(),
				parent == null ? null : parent.id(), controlRef, controlRef + " title", controlRef + " statement",
				CREATED_AT, "me");
	}

	private static ControlMappingApiMappingResponseDto mapping(UUID requirementId, UUID controlId) {
		return new ControlMappingApiMappingResponseDto(UUID.randomUUID(), requirementId, controlId, "Because",
				CREATED_AT, "me");
	}

}
