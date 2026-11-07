package io.github.gracegrc.adminconsoleservice.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardAuthority;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardControl;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardControlGroup;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardCoverage;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardFile;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardFramework;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardMappedControl;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardMappedRequirement;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardRequirement;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFileResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFrameworkResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionRequirementResponseDto;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DashboardMapperTest {

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

	@Test
	void toResponseDtoNestsRequirementsByTheirParent() {
		DashboardFile doraText = doraFile(example(), DORA_TEXT);

		assertThat(doraText.requirements()).extracting(DashboardRequirement::provisionRef)
			.containsExactly("Art. 5", "Art. 6");
		DashboardRequirement art5 = doraText.requirements().getFirst();
		assertThat(art5.children()).extracting(DashboardRequirement::provisionRef).containsExactly("Art. 5(1)");
		assertThat(art5.children().getFirst().children()).extracting(DashboardRequirement::provisionRef)
			.containsExactly("Art. 5(1)(a)", "Art. 5(1)(b)");
		assertThat(art5.children().getFirst().children()).allSatisfy(leaf -> assertThat(leaf.children()).isEmpty());
	}

	@Test
	void toResponseDtoCountsTheMappedLowestLevelProvisionsPerFileAndFramework() {
		DashboardFramework dora = framework(example(), DORA);

		assertThat(dora.files()).extracting(DashboardFile::id, DashboardFile::coverage)
			.containsExactly(tuple(DORA_TEXT.id(), new DashboardCoverage(1, 3)),
					tuple(DORA_RTS.id(), new DashboardCoverage(1, 1)),
					tuple(DORA_ANNEX.id(), new DashboardCoverage(0, 0)));
		assertThat(dora.coverage()).isEqualTo(new DashboardCoverage(2, 4));
	}

	@Test
	void toResponseDtoListsTheMappedControlsOfEveryRequirement() {
		DashboardFile doraText = doraFile(example(), DORA_TEXT);

		DashboardRequirement art5 = doraText.requirements().getFirst();
		assertThat(art5.mappedControls()).containsExactly(new DashboardMappedControl(ART_5_TO_AC_2.id(),
				AC_2.id(), "AC-2", "AC-2 title", "Because"));
		assertThat(art5.children().getFirst().children().getFirst().mappedControls())
			.extracting(DashboardMappedControl::mappingId)
			.containsExactly(ART_5_1_A_TO_AC_2.id(), ART_5_1_A_TO_AC_3.id());
		assertThat(doraText.requirements().get(1).mappedControls()).isEmpty();
	}

	@Test
	void toResponseDtoNestsGroupsControlsAndEnhancements() {
		DashboardResponseDto dashboard = example();

		assertThat(dashboard.controlGroups()).extracting(DashboardControlGroup::groupRef)
			.containsExactly("AC", "AU");
		DashboardControlGroup ac = dashboard.controlGroups().getFirst();
		assertThat(ac.controls()).extracting(DashboardControl::controlRef).containsExactly("AC-2");
		assertThat(ac.controls().getFirst().enhancements()).extracting(DashboardControl::controlRef)
			.containsExactly("AC-2(1)");
		assertThat(ac.children()).extracting(DashboardControlGroup::groupRef).containsExactly("AC.1");
		assertThat(ac.children().getFirst().controls()).extracting(DashboardControl::controlRef)
			.containsExactly("AC-3");
		assertThat(dashboard.controlGroups().get(1).controls()).isEmpty();
		assertThat(dashboard.ungroupedControls()).extracting(DashboardControl::controlRef)
			.containsExactly("PM-1");
		assertThat(dashboard.ungroupedControls().getFirst().enhancements())
			.extracting(DashboardControl::controlRef)
			.containsExactly("PM-1(1)");
	}

	@Test
	void toResponseDtoListsTheMappedRequirementsOfEveryControlWithoutOrphans() {
		DashboardControl ac2 = example().controlGroups().getFirst().controls().getFirst();

		assertThat(ac2.mappedRequirements()).containsExactly(
				new DashboardMappedRequirement(ART_5_TO_AC_2.id(), ART_5.id(), "Art. 5", "Art. 5 heading",
						DORA_TEXT.id(), "DORA text", DORA.id(), "DORA", EBA.id(), "EBA", "Because"),
				new DashboardMappedRequirement(ART_5_1_A_TO_AC_2.id(), ART_5_1_A.id(), "Art. 5(1)(a)",
						"Art. 5(1)(a) heading", DORA_TEXT.id(), "DORA text", DORA.id(), "DORA", EBA.id(), "EBA",
						"Because"));
		assertThat(ac2.enhancements().getFirst().mappedRequirements())
			.extracting(DashboardMappedRequirement::mappingId)
			.containsExactly(RTS_1_TO_AC_2_1.id());
	}

	@Test
	void toResponseDtoSortsAuthoritiesFrameworksAndFiles() {
		DashboardResponseDto dashboard = example();

		assertThat(dashboard.authorities()).extracting(DashboardAuthority::name).containsExactly("BaFin", "EBA");
		assertThat(dashboard.authorities().getFirst().frameworks()).isEmpty();
		DashboardAuthority eba = dashboard.authorities().get(1);
		assertThat(eba.frameworks()).extracting(DashboardFramework::name).containsExactly("DORA", "MiCA");
		assertThat(eba.frameworks().get(1).files()).isEmpty();
		assertThat(eba.frameworks().get(1).coverage()).isEqualTo(new DashboardCoverage(0, 0));
		assertThat(eba.frameworks().getFirst().files()).extracting(DashboardFile::title)
			.containsExactly("DORA text", "RTS", "Annex");
	}

	@Test
	void toResponseDtoCopiesTheUpstreamFields() {
		DashboardResponseDto dashboard = example();

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
	void toResponseDtoLeavesOutMappingsOfRequirementsWhoseFileFrameworkOrAuthorityIsMissing() {
		RegulatoryIngestionFrameworkResponseDto withoutAuthority = framework(BAFIN, "PSD3");
		RegulatoryIngestionFileResponseDto withoutFramework = file(MICA, "MiCA text", null);
		RegulatoryIngestionFileResponseDto inFrameworkWithoutAuthority = file(withoutAuthority, "PSD3 text", null);
		RegulatoryIngestionRequirementResponseDto withoutFile = requirement(DORA_TEXT, null, "Art. 1");
		RegulatoryIngestionRequirementResponseDto inFileWithoutFramework = requirement(withoutFramework, null, "Art. 2");
		RegulatoryIngestionRequirementResponseDto inFrameworkWithoutAuthorityRequirement = requirement(
				inFrameworkWithoutAuthority, null, "Art. 3");
		DashboardResponseDto dashboard = DashboardMapper.toResponseDto(List.of(EBA), List.of(withoutAuthority),
				List.of(withoutFramework, inFrameworkWithoutAuthority),
				List.of(withoutFile, inFileWithoutFramework, inFrameworkWithoutAuthorityRequirement),
				List.of(mapping(withoutFile.id(), PM_1.id()), mapping(inFileWithoutFramework.id(), PM_1.id()),
						mapping(inFrameworkWithoutAuthorityRequirement.id(), PM_1.id())),
				List.of(), List.of(PM_1));

		assertThat(dashboard.authorities()).extracting(DashboardAuthority::frameworks)
			.containsExactly(List.of());
		assertThat(dashboard.ungroupedControls().getFirst().mappedRequirements()).isEmpty();
	}

	@Test
	void toResponseDtoLeavesOutFrameworksAndFilesWhoseParentIsMissing() {
		RegulatoryIngestionFrameworkResponseDto withoutAuthority = framework(BAFIN, "PSD3");
		RegulatoryIngestionFileResponseDto withoutFramework = file(MICA, "MiCA text", null);
		DashboardResponseDto dashboard = DashboardMapper.toResponseDto(List.of(EBA), List.of(withoutAuthority, DORA),
				List.of(withoutFramework, DORA_RTS), List.of(requirement(withoutFramework, null, "Art. 2"), RTS_1),
				List.of(), List.of(), List.of());

		assertThat(dashboard.authorities()).singleElement()
			.extracting(DashboardAuthority::frameworks)
			.satisfies(frameworks -> assertThat(frameworks).extracting(DashboardFramework::name)
				.containsExactly("DORA"));
		assertThat(framework(dashboard, DORA).files()).extracting(DashboardFile::title).containsExactly("RTS");
	}

	@Test
	void toResponseDtoWithEmptyUpstreamListsReturnsEmptyArrays() {
		DashboardResponseDto dashboard = DashboardMapper.toResponseDto(List.of(), List.of(), List.of(), List.of(),
				List.of(), List.of(), List.of());

		assertThat(dashboard)
			.isEqualTo(new DashboardResponseDto(List.of(), List.of(), List.of()));
	}

	private static DashboardResponseDto example() {
		return DashboardMapper.toResponseDto(List.of(EBA, BAFIN), List.of(MICA, DORA),
				List.of(DORA_ANNEX, DORA_RTS, DORA_TEXT), List.of(ART_6, ART_5_1_B, RTS_1, ART_5_1_A, ART_5_1, ART_5),
				List.of(UNKNOWN_REQUIREMENT_TO_AC_2, ART_5_1_A_TO_AC_3, ART_6_TO_UNKNOWN_CONTROL, RTS_1_TO_AC_2_1,
						ART_5_TO_AC_2, ART_5_1_A_TO_AC_2),
				List.of(AU, AC_1, AC), List.of(PM_1_1, AC_3, PM_1, AC_2_1, AC_2));
	}

	private static DashboardFramework framework(DashboardResponseDto dashboard,
			RegulatoryIngestionFrameworkResponseDto framework) {
		return dashboard.authorities()
			.stream()
			.flatMap(authority -> authority.frameworks().stream())
			.filter(candidate -> candidate.id().equals(framework.id()))
			.findFirst()
			.orElseThrow();
	}

	private static DashboardFile doraFile(DashboardResponseDto dashboard,
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
