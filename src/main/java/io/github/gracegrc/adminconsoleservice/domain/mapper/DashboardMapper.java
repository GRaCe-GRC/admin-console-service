package io.github.gracegrc.adminconsoleservice.domain.mapper;

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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DashboardMapper {

	private static final Comparator<RegulatoryAuthorityResponseDto> AUTHORITY_ORDER = Comparator
		.comparing(RegulatoryAuthorityResponseDto::name)
		.thenComparing(RegulatoryAuthorityResponseDto::id);

	private static final Comparator<RegulatoryIngestionFrameworkResponseDto> FRAMEWORK_ORDER = Comparator
		.comparing(RegulatoryIngestionFrameworkResponseDto::name)
		.thenComparing(RegulatoryIngestionFrameworkResponseDto::id);

	private static final Comparator<RegulatoryIngestionFileResponseDto> FILE_ORDER = Comparator
		.comparing(RegulatoryIngestionFileResponseDto::publicationDate, Comparator.nullsLast(Comparator.naturalOrder()))
		.thenComparing(RegulatoryIngestionFileResponseDto::title)
		.thenComparing(RegulatoryIngestionFileResponseDto::id);

	private static final Comparator<RegulatoryIngestionRequirementResponseDto> REQUIREMENT_ORDER = Comparator
		.comparing(RegulatoryIngestionRequirementResponseDto::provisionRef)
		.thenComparing(RegulatoryIngestionRequirementResponseDto::id);

	private static final Comparator<ControlApiControlGroupResponseDto> GROUP_ORDER = Comparator
		.comparing(ControlApiControlGroupResponseDto::groupRef)
		.thenComparing(ControlApiControlGroupResponseDto::id);

	private static final Comparator<ControlApiControlResponseDto> CONTROL_ORDER = Comparator
		.comparing(ControlApiControlResponseDto::controlRef)
		.thenComparing(ControlApiControlResponseDto::id);

	private static final Comparator<DashboardMappedControl> MAPPED_CONTROL_ORDER = Comparator
		.comparing(DashboardMappedControl::controlRef)
		.thenComparing(DashboardMappedControl::mappingId);

	private static final Comparator<DashboardMappedRequirement> MAPPED_REQUIREMENT_ORDER = Comparator
		.comparing(DashboardMappedRequirement::provisionRef)
		.thenComparing(DashboardMappedRequirement::mappingId);

	private DashboardMapper() {
	}

	public static DashboardResponseDto toResponseDto(List<RegulatoryAuthorityResponseDto> authorities,
			List<RegulatoryIngestionFrameworkResponseDto> frameworks, List<RegulatoryIngestionFileResponseDto> files,
			List<RegulatoryIngestionRequirementResponseDto> requirements,
			List<ControlMappingApiMappingResponseDto> mappings, List<ControlApiControlGroupResponseDto> groups,
			List<ControlApiControlResponseDto> controls) {
		Map<UUID, DashboardMappedRequirement> requirementReferences = requirementReferences(authorities, frameworks,
				files, requirements);
		Map<UUID, ControlApiControlResponseDto> controlById = byId(controls, ControlApiControlResponseDto::id);
		List<ControlMappingApiMappingResponseDto> placedMappings = mappings.stream()
			.filter(mapping -> requirementReferences.containsKey(mapping.requirementId())
					&& controlById.containsKey(mapping.controlId()))
			.toList();
		Map<UUID, List<DashboardMappedControl>> mappedControlsByRequirement = mappedControlsByRequirement(
				placedMappings, controlById);
		Map<UUID, List<DashboardRequirement>> requirementTreesByFile = requirementTreesByFile(requirements,
				mappedControlsByRequirement);
		Map<UUID, DashboardCoverage> coverageByFile = coverageByFile(requirements,
				mappedControlsByRequirement.keySet());
		Map<UUID, List<DashboardFile>> filesByFramework = mapValues(
				groupedBy(files.stream().filter(file -> file.frameworkId() != null),
						RegulatoryIngestionFileResponseDto::frameworkId, FILE_ORDER),
				file -> toFile(file, requirementTreesByFile, coverageByFile));
		Map<UUID, List<DashboardFramework>> frameworksByAuthority = mapValues(
				groupedBy(frameworks.stream().filter(framework -> framework.authorityId() != null),
						RegulatoryIngestionFrameworkResponseDto::authorityId, FRAMEWORK_ORDER),
				framework -> toFramework(framework, filesByFramework));
		Map<UUID, List<DashboardMappedRequirement>> mappedRequirementsByControl = mappedRequirementsByControl(
				placedMappings, requirementReferences);
		Map<UUID, List<ControlApiControlResponseDto>> enhancementsByParent = groupedBy(
				controls.stream().filter(control -> controlById.containsKey(control.parentControlId())),
				ControlApiControlResponseDto::parentControlId, CONTROL_ORDER);
		Set<UUID> groupIds = groups.stream().map(ControlApiControlGroupResponseDto::id).collect(Collectors.toSet());
		List<ControlApiControlResponseDto> topLevelControls = controls.stream()
			.filter(control -> !controlById.containsKey(control.parentControlId()))
			.sorted(CONTROL_ORDER)
			.toList();
		Map<UUID, List<DashboardControl>> controlsByGroup = mapValues(
				groupedBy(topLevelControls.stream().filter(control -> groupIds.contains(control.groupId())),
						ControlApiControlResponseDto::groupId, CONTROL_ORDER),
				control -> toControl(control, enhancementsByParent, mappedRequirementsByControl));
		Map<UUID, List<ControlApiControlGroupResponseDto>> groupsByParent = groupedBy(
				groups.stream().filter(group -> groupIds.contains(group.parentGroupId())),
				ControlApiControlGroupResponseDto::parentGroupId, GROUP_ORDER);
		return new DashboardResponseDto(
				authorities.stream()
					.sorted(AUTHORITY_ORDER)
					.map(authority -> toAuthority(authority, frameworksByAuthority))
					.toList(),
				groups.stream()
					.filter(group -> !groupIds.contains(group.parentGroupId()))
					.sorted(GROUP_ORDER)
					.map(group -> toGroup(group, groupsByParent, controlsByGroup))
					.toList(),
				topLevelControls.stream()
					.filter(control -> !groupIds.contains(control.groupId()))
					.map(control -> toControl(control, enhancementsByParent, mappedRequirementsByControl))
					.toList());
	}

	private static Map<UUID, DashboardMappedRequirement> requirementReferences(
			List<RegulatoryAuthorityResponseDto> authorities, List<RegulatoryIngestionFrameworkResponseDto> frameworks,
			List<RegulatoryIngestionFileResponseDto> files,
			List<RegulatoryIngestionRequirementResponseDto> requirements) {
		Map<UUID, RegulatoryAuthorityResponseDto> authorityById = byId(authorities, RegulatoryAuthorityResponseDto::id);
		Map<UUID, RegulatoryIngestionFrameworkResponseDto> frameworkById = byId(frameworks,
				RegulatoryIngestionFrameworkResponseDto::id);
		Map<UUID, RegulatoryIngestionFileResponseDto> fileById = byId(files, RegulatoryIngestionFileResponseDto::id);
		Map<UUID, DashboardMappedRequirement> references = new HashMap<>();
		for (RegulatoryIngestionRequirementResponseDto requirement : requirements) {
			RegulatoryIngestionFileResponseDto file = fileById.get(requirement.fileId());
			RegulatoryIngestionFrameworkResponseDto framework = file == null ? null
					: frameworkById.get(file.frameworkId());
			RegulatoryAuthorityResponseDto authority = framework == null ? null
					: authorityById.get(framework.authorityId());
			if (authority != null) {
				references.putIfAbsent(requirement.id(),
						new DashboardMappedRequirement(null, requirement.id(), requirement.provisionRef(),
								requirement.heading(), file.id(), file.title(), framework.id(), framework.name(),
								authority.id(), authority.name(), null));
			}
		}
		return references;
	}

	private static Map<UUID, List<DashboardMappedControl>> mappedControlsByRequirement(
			List<ControlMappingApiMappingResponseDto> placedMappings,
			Map<UUID, ControlApiControlResponseDto> controlById) {
		return placedMappings.stream()
			.collect(Collectors.groupingBy(ControlMappingApiMappingResponseDto::requirementId,
					Collectors.collectingAndThen(Collectors.toList(), requirementMappings -> requirementMappings
						.stream()
						.map(mapping -> toMappedControl(mapping, controlById.get(mapping.controlId())))
						.sorted(MAPPED_CONTROL_ORDER)
						.toList())));
	}

	private static Map<UUID, List<DashboardMappedRequirement>> mappedRequirementsByControl(
			List<ControlMappingApiMappingResponseDto> placedMappings,
			Map<UUID, DashboardMappedRequirement> requirementReferences) {
		return placedMappings.stream()
			.collect(Collectors.groupingBy(ControlMappingApiMappingResponseDto::controlId,
					Collectors.collectingAndThen(Collectors.toList(), controlMappings -> controlMappings.stream()
						.map(mapping -> toMappedRequirement(mapping,
								requirementReferences.get(mapping.requirementId())))
						.sorted(MAPPED_REQUIREMENT_ORDER)
						.toList())));
	}

	private static Map<UUID, List<DashboardRequirement>> requirementTreesByFile(
			List<RegulatoryIngestionRequirementResponseDto> requirements,
			Map<UUID, List<DashboardMappedControl>> mappedControlsByRequirement) {
		Set<UUID> requirementIds = requirements.stream()
			.map(RegulatoryIngestionRequirementResponseDto::id)
			.collect(Collectors.toSet());
		Map<UUID, List<RegulatoryIngestionRequirementResponseDto>> requirementsByParent = groupedBy(
				requirements.stream().filter(requirement -> requirementIds.contains(requirement.parentRequirementId())),
				RegulatoryIngestionRequirementResponseDto::parentRequirementId, REQUIREMENT_ORDER);
		return mapValues(groupedBy(
				requirements.stream()
					.filter(requirement -> requirement.fileId() != null
							&& !requirementIds.contains(requirement.parentRequirementId())),
				RegulatoryIngestionRequirementResponseDto::fileId, REQUIREMENT_ORDER),
				requirement -> toRequirement(requirement, requirementsByParent, mappedControlsByRequirement));
	}

	private static Map<UUID, DashboardCoverage> coverageByFile(
			List<RegulatoryIngestionRequirementResponseDto> requirements, Set<UUID> mappedRequirementIds) {
		Set<UUID> requirementIds = requirements.stream()
			.map(RegulatoryIngestionRequirementResponseDto::id)
			.collect(Collectors.toSet());
		Set<UUID> parentIds = requirements.stream()
			.map(RegulatoryIngestionRequirementResponseDto::parentRequirementId)
			.filter(requirementIds::contains)
			.collect(Collectors.toSet());
		return requirements.stream()
			.filter(requirement -> requirement.fileId() != null && !parentIds.contains(requirement.id()))
			.collect(Collectors.groupingBy(RegulatoryIngestionRequirementResponseDto::fileId,
					Collectors.collectingAndThen(Collectors.toList(),
							leaves -> new DashboardCoverage(
									(int) leaves.stream().filter(leaf -> mappedRequirementIds.contains(leaf.id())).count(),
									leaves.size()))));
	}

	private static DashboardAuthority toAuthority(RegulatoryAuthorityResponseDto authority,
			Map<UUID, List<DashboardFramework>> frameworksByAuthority) {
		return new DashboardAuthority(authority.id(), authority.name(), authority.jurisdiction(), authority.enabled(),
				authority.createdAt(), authority.createdBy(), children(frameworksByAuthority, authority.id()));
	}

	private static DashboardFramework toFramework(RegulatoryIngestionFrameworkResponseDto framework,
			Map<UUID, List<DashboardFile>> filesByFramework) {
		List<DashboardFile> files = children(filesByFramework, framework.id());
		DashboardCoverage coverage = new DashboardCoverage(
				files.stream().mapToInt(file -> file.coverage().mapped()).sum(),
				files.stream().mapToInt(file -> file.coverage().total()).sum());
		return new DashboardFramework(framework.id(), framework.name(), framework.shortName(), framework.citation(),
				framework.enabled(), framework.createdAt(), framework.createdBy(), coverage, files);
	}

	private static DashboardFile toFile(RegulatoryIngestionFileResponseDto file,
			Map<UUID, List<DashboardRequirement>> requirementTreesByFile, Map<UUID, DashboardCoverage> coverageByFile) {
		return new DashboardFile(file.id(), file.title(), file.sourceUrl(), file.versionLabel(), file.publicationDate(),
				file.createdAt(), file.createdBy(), coverageByFile.getOrDefault(file.id(), new DashboardCoverage(0, 0)),
				children(requirementTreesByFile, file.id()));
	}

	private static DashboardRequirement toRequirement(RegulatoryIngestionRequirementResponseDto requirement,
			Map<UUID, List<RegulatoryIngestionRequirementResponseDto>> requirementsByParent,
			Map<UUID, List<DashboardMappedControl>> mappedControlsByRequirement) {
		return new DashboardRequirement(requirement.id(), requirement.provisionRef(), requirement.heading(),
				requirement.text(), requirement.createdAt(), requirement.createdBy(),
				children(mappedControlsByRequirement, requirement.id()),
				children(requirementsByParent, requirement.id()).stream()
					.map(child -> toRequirement(child, requirementsByParent, mappedControlsByRequirement))
					.toList());
	}

	private static DashboardMappedControl toMappedControl(ControlMappingApiMappingResponseDto mapping,
			ControlApiControlResponseDto control) {
		return new DashboardMappedControl(mapping.id(), control.id(), control.controlRef(), control.title(),
				mapping.rationale());
	}

	private static DashboardMappedRequirement toMappedRequirement(ControlMappingApiMappingResponseDto mapping,
			DashboardMappedRequirement reference) {
		return new DashboardMappedRequirement(mapping.id(), reference.requirementId(), reference.provisionRef(),
				reference.heading(), reference.fileId(), reference.fileTitle(), reference.frameworkId(),
				reference.frameworkName(), reference.authorityId(), reference.authorityName(), mapping.rationale());
	}

	private static DashboardControlGroup toGroup(ControlApiControlGroupResponseDto group,
			Map<UUID, List<ControlApiControlGroupResponseDto>> groupsByParent,
			Map<UUID, List<DashboardControl>> controlsByGroup) {
		return new DashboardControlGroup(group.id(), group.groupRef(), group.title(), group.createdAt(),
				group.createdBy(), children(controlsByGroup, group.id()),
				children(groupsByParent, group.id()).stream()
					.map(child -> toGroup(child, groupsByParent, controlsByGroup))
					.toList());
	}

	private static DashboardControl toControl(ControlApiControlResponseDto control,
			Map<UUID, List<ControlApiControlResponseDto>> enhancementsByParent,
			Map<UUID, List<DashboardMappedRequirement>> mappedRequirementsByControl) {
		return new DashboardControl(control.id(), control.controlRef(), control.title(), control.statement(),
				control.createdAt(), control.createdBy(), children(mappedRequirementsByControl, control.id()),
				children(enhancementsByParent, control.id()).stream()
					.map(enhancement -> toControl(enhancement, enhancementsByParent, mappedRequirementsByControl))
					.toList());
	}

	private static <T> Map<UUID, T> byId(List<T> items, Function<T, UUID> id) {
		return items.stream().collect(Collectors.toMap(id, Function.identity(), (first, second) -> first));
	}

	private static <T> Map<UUID, List<T>> groupedBy(Stream<T> items, Function<T, UUID> key, Comparator<T> order) {
		return items.sorted(order).collect(Collectors.groupingBy(key));
	}

	private static <T, R> Map<UUID, List<R>> mapValues(Map<UUID, List<T>> byKey, Function<T, R> mapper) {
		return byKey.entrySet()
			.stream()
			.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().map(mapper).toList()));
	}

	private static <T> List<T> children(Map<UUID, List<T>> byParent, UUID parentId) {
		return byParent.getOrDefault(parentId, List.of());
	}

}
