package io.github.gracegrc.adminconsoleservice.domain.mapper;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardAuthority;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardFile;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardFramework;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardRequirement;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFileResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFrameworkResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionRequirementResponseDto;
import java.util.Comparator;
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

	private DashboardMapper() {
	}

	public static DashboardResponseDto toResponseDto(List<RegulatoryAuthorityResponseDto> authorities,
			List<RegulatoryIngestionFrameworkResponseDto> frameworks, List<RegulatoryIngestionFileResponseDto> files,
			List<RegulatoryIngestionRequirementResponseDto> requirements) {
		Map<UUID, List<DashboardRequirement>> requirementTreesByFile = requirementTreesByFile(requirements);
		Map<UUID, List<DashboardFile>> filesByFramework = mapValues(
				groupedBy(files.stream().filter(file -> file.frameworkId() != null),
						RegulatoryIngestionFileResponseDto::frameworkId, FILE_ORDER),
				file -> toFile(file, requirementTreesByFile));
		Map<UUID, List<DashboardFramework>> frameworksByAuthority = mapValues(
				groupedBy(frameworks.stream().filter(framework -> framework.authorityId() != null),
						RegulatoryIngestionFrameworkResponseDto::authorityId, FRAMEWORK_ORDER),
				framework -> toFramework(framework, filesByFramework));
		return new DashboardResponseDto(authorities.stream()
			.sorted(AUTHORITY_ORDER)
			.map(authority -> toAuthority(authority, frameworksByAuthority))
			.toList());
	}

	private static Map<UUID, List<DashboardRequirement>> requirementTreesByFile(
			List<RegulatoryIngestionRequirementResponseDto> requirements) {
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
				requirement -> toRequirement(requirement, requirementsByParent));
	}

	private static DashboardAuthority toAuthority(RegulatoryAuthorityResponseDto authority,
			Map<UUID, List<DashboardFramework>> frameworksByAuthority) {
		return new DashboardAuthority(authority.id(), authority.name(), authority.jurisdiction(), authority.enabled(),
				authority.createdAt(), authority.createdBy(), children(frameworksByAuthority, authority.id()));
	}

	private static DashboardFramework toFramework(RegulatoryIngestionFrameworkResponseDto framework,
			Map<UUID, List<DashboardFile>> filesByFramework) {
		return new DashboardFramework(framework.id(), framework.name(), framework.shortName(), framework.citation(),
				framework.enabled(), framework.createdAt(), framework.createdBy(),
				children(filesByFramework, framework.id()));
	}

	private static DashboardFile toFile(RegulatoryIngestionFileResponseDto file,
			Map<UUID, List<DashboardRequirement>> requirementTreesByFile) {
		return new DashboardFile(file.id(), file.title(), file.sourceUrl(), file.versionLabel(), file.publicationDate(),
				file.createdAt(), file.createdBy(), children(requirementTreesByFile, file.id()));
	}

	private static DashboardRequirement toRequirement(RegulatoryIngestionRequirementResponseDto requirement,
			Map<UUID, List<RegulatoryIngestionRequirementResponseDto>> requirementsByParent) {
		return new DashboardRequirement(requirement.id(), requirement.provisionRef(), requirement.heading(),
				requirement.text(), requirement.createdAt(), requirement.createdBy(),
				children(requirementsByParent, requirement.id()).stream()
					.map(child -> toRequirement(child, requirementsByParent))
					.toList());
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
