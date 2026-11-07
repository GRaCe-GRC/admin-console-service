package io.github.gracegrc.adminconsoleservice.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardAuthority;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardCoverage;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardFile;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardFramework;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardMappedControl;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardRequirement;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityResponseDto;
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

	private static final UUID CONTROL_1 = UUID.randomUUID();

	private static final UUID CONTROL_2 = UUID.randomUUID();

	private static final ControlMappingApiMappingResponseDto ART_5_1_A_TO_CONTROL_1 = mapping(ART_5_1_A.id(), CONTROL_1,
			"2026-10-02T08:00:00Z");

	private static final ControlMappingApiMappingResponseDto ART_5_1_A_TO_CONTROL_2 = mapping(ART_5_1_A.id(), CONTROL_2,
			"2026-10-01T08:00:00Z");

	private static final ControlMappingApiMappingResponseDto ART_5_TO_CONTROL_1 = mapping(ART_5.id(), CONTROL_1,
			"2026-10-01T08:00:00Z");

	private static final ControlMappingApiMappingResponseDto RTS_1_TO_CONTROL_2 = mapping(RTS_1.id(), CONTROL_2,
			"2026-10-01T08:00:00Z");

	private static final ControlMappingApiMappingResponseDto UNKNOWN_REQUIREMENT_TO_CONTROL_1 = mapping(UUID.randomUUID(),
			CONTROL_1, "2026-10-01T08:00:00Z");

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
		assertThat(doraText.requirements().get(1).children()).isEmpty();
	}

	@Test
	void toResponseDtoCountsTheMappedLowestLevelProvisionsPerFileAndFramework() {
		DashboardFramework dora = framework(example(), DORA);

		assertThat(dora.files()).extracting(DashboardFile::id, DashboardFile::coverage)
			.containsExactly(tuple(DORA_TEXT.id(), new DashboardCoverage(1, 3)),
					tuple(DORA_RTS.id(), new DashboardCoverage(1, 1)),
					tuple(DORA_ANNEX.id(), new DashboardCoverage(0, 0)));
		assertThat(dora.coverage()).isEqualTo(new DashboardCoverage(2, 4));
		assertThat(framework(example(), MICA).coverage())
			.isEqualTo(new DashboardCoverage(0, 0));
	}

	@Test
	void toResponseDtoListsTheMappedControlsOfEveryRequirement() {
		DashboardFile doraText = doraFile(example(), DORA_TEXT);

		DashboardRequirement art5 = doraText.requirements().getFirst();
		assertThat(art5.mappedControls()).containsExactly(
				new DashboardMappedControl(ART_5_TO_CONTROL_1.id(), CONTROL_1, "Because"));
		assertThat(art5.children().getFirst().children().getFirst().mappedControls())
			.extracting(DashboardMappedControl::mappingId)
			.containsExactly(ART_5_1_A_TO_CONTROL_2.id(), ART_5_1_A_TO_CONTROL_1.id());
		assertThat(doraText.requirements().get(1).mappedControls()).isEmpty();
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
		DashboardResponseDto dashboard = DashboardMapper.toResponseDto(List.of(EBA), List.of(withoutAuthority, DORA),
				List.of(withoutFramework, inFrameworkWithoutAuthority, DORA_RTS),
				List.of(withoutFile, inFileWithoutFramework, inFrameworkWithoutAuthorityRequirement, RTS_1),
				List.of(mapping(withoutFile.id(), CONTROL_1, "2026-10-01T08:00:00Z"),
						mapping(inFileWithoutFramework.id(), CONTROL_1, "2026-10-01T08:00:00Z"),
						mapping(inFrameworkWithoutAuthorityRequirement.id(), CONTROL_1, "2026-10-01T08:00:00Z"),
						UNKNOWN_REQUIREMENT_TO_CONTROL_1));

		DashboardFramework dora = framework(dashboard, DORA);

		assertThat(dora.coverage()).isEqualTo(new DashboardCoverage(0, 1));
		assertThat(dora.files().getFirst().requirements().getFirst().mappedControls()).isEmpty();
	}

	@Test
	void toResponseDtoSortsAuthoritiesFrameworksAndFiles() {
		DashboardResponseDto dashboard = example();

		assertThat(dashboard.authorities()).extracting(DashboardAuthority::name).containsExactly("BaFin", "EBA");
		assertThat(dashboard.authorities().getFirst().frameworks()).isEmpty();
		DashboardAuthority eba = dashboard.authorities().get(1);
		assertThat(eba.frameworks()).extracting(DashboardFramework::name).containsExactly("DORA", "MiCA");
		assertThat(eba.frameworks().get(1).files()).isEmpty();
		assertThat(eba.frameworks().getFirst().files()).extracting(DashboardFile::title)
			.containsExactly("DORA text", "RTS", "Annex");
		assertThat(doraFile(dashboard, DORA_ANNEX).requirements()).isEmpty();
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
	}

	@Test
	void toResponseDtoLeavesOutFrameworksAndFilesWhoseParentIsMissing() {
		RegulatoryIngestionFrameworkResponseDto withoutAuthority = framework(BAFIN, "PSD3");
		RegulatoryIngestionFileResponseDto withoutFramework = file(MICA, "MiCA text", null);
		DashboardResponseDto dashboard = DashboardMapper.toResponseDto(List.of(EBA), List.of(withoutAuthority, DORA),
				List.of(withoutFramework, DORA_RTS), List.of(requirement(withoutFramework, null, "Art. 2"), RTS_1),
				List.of());

		assertThat(dashboard.authorities()).singleElement()
			.extracting(DashboardAuthority::frameworks)
			.satisfies(frameworks -> assertThat(frameworks).extracting(DashboardFramework::name)
				.containsExactly("DORA"));
		assertThat(framework(dashboard, DORA).files()).extracting(DashboardFile::title).containsExactly("RTS");
	}

	@Test
	void toResponseDtoWithEmptyUpstreamListsReturnsEmptyArrays() {
		DashboardResponseDto dashboard = DashboardMapper.toResponseDto(List.of(), List.of(), List.of(), List.of(), List.of());

		assertThat(dashboard).isEqualTo(new DashboardResponseDto(List.of()));
	}

	private static DashboardResponseDto example() {
		return DashboardMapper.toResponseDto(List.of(EBA, BAFIN), List.of(MICA, DORA),
				List.of(DORA_ANNEX, DORA_RTS, DORA_TEXT), List.of(ART_6, ART_5_1_B, RTS_1, ART_5_1_A, ART_5_1, ART_5),
				List.of(UNKNOWN_REQUIREMENT_TO_CONTROL_1, ART_5_1_A_TO_CONTROL_1, RTS_1_TO_CONTROL_2, ART_5_TO_CONTROL_1,
						ART_5_1_A_TO_CONTROL_2));
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

	private static ControlMappingApiMappingResponseDto mapping(UUID requirementId, UUID controlId, String createdAt) {
		return new ControlMappingApiMappingResponseDto(UUID.randomUUID(), requirementId, controlId, "Because",
				Instant.parse(createdAt), "me");
	}

}
