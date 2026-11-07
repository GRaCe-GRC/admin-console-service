package io.github.gracegrc.adminconsoleservice.domain.service;

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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

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

	private static final Comparator<DashboardMappedControlResponseDto> MAPPED_CONTROL_ORDER = Comparator
		.comparing(DashboardMappedControlResponseDto::controlRef)
		.thenComparing(DashboardMappedControlResponseDto::mappingId);

	private static final Comparator<DashboardMappedRequirementResponseDto> MAPPED_REQUIREMENT_ORDER = Comparator
		.comparing(DashboardMappedRequirementResponseDto::provisionRef)
		.thenComparing(DashboardMappedRequirementResponseDto::mappingId);

	private final RegulatoryIngestionService regulatoryIngestionService;

	private final ControlApiService controlApiService;

	private final ControlMappingApiService controlMappingApiService;

	private final Executor dashboardExecutor;

	public DashboardServiceImpl(RegulatoryIngestionService regulatoryIngestionService,
			ControlApiService controlApiService, ControlMappingApiService controlMappingApiService,
			Executor dashboardExecutor) {
		this.regulatoryIngestionService = regulatoryIngestionService;
		this.controlApiService = controlApiService;
		this.controlMappingApiService = controlMappingApiService;
		this.dashboardExecutor = dashboardExecutor;
	}

	@Override
	public DashboardResponseDto findDashboard() {
		CompletableFuture<List<RegulatoryAuthorityResponseDto>> authorities = fetch(
				regulatoryIngestionService::findAllAuthorities);
		CompletableFuture<List<RegulatoryIngestionFrameworkResponseDto>> frameworks = fetch(
				regulatoryIngestionService::findAllFrameworks);
		CompletableFuture<List<RegulatoryIngestionFileResponseDto>> files = fetch(regulatoryIngestionService::findAllFiles);
		CompletableFuture<List<RegulatoryIngestionRequirementResponseDto>> requirements = fetch(
				regulatoryIngestionService::findAllRequirements);
		CompletableFuture<List<ControlApiControlGroupResponseDto>> groups = fetch(controlApiService::findAllControlGroups);
		CompletableFuture<List<ControlApiControlResponseDto>> controls = fetch(controlApiService::findAllControls);
		CompletableFuture<List<ControlMappingApiMappingResponseDto>> mappings = fetch(
				controlMappingApiService::findAllMappings);
		return new Assembly(join(authorities), join(frameworks), join(files), join(requirements), join(groups),
				join(controls), join(mappings))
			.dashboard();
	}

	private <T> CompletableFuture<List<T>> fetch(Supplier<List<T>> call) {
		return CompletableFuture.supplyAsync(() -> Objects.requireNonNullElse(call.get(), List.<T>of()),
				dashboardExecutor);
	}

	private static <T> T join(CompletableFuture<T> future) {
		try {
			return future.join();
		}
		catch (CompletionException ex) {
			if (ex.getCause() instanceof RuntimeException cause) {
				throw cause;
			}
			throw ex;
		}
	}

	private static <T> Map<UUID, T> byId(List<T> items, Function<T, UUID> id) {
		return items.stream().collect(Collectors.toMap(id, Function.identity(), (first, second) -> first));
	}

	private static <T> Map<UUID, List<T>> groupedBy(List<T> items, Function<T, UUID> key, Predicate<T> filter,
			Comparator<T> order) {
		return items.stream().filter(filter).sorted(order).collect(Collectors.groupingBy(key));
	}

	private static <T> List<T> children(Map<UUID, List<T>> byParent, UUID parentId) {
		return byParent.getOrDefault(parentId, List.of());
	}

	private static final class Assembly {

		private final List<RegulatoryAuthorityResponseDto> authorities;

		private final List<ControlApiControlGroupResponseDto> groups;

		private final List<ControlApiControlResponseDto> controls;

		private final Map<UUID, RegulatoryAuthorityResponseDto> authorityById;

		private final Map<UUID, RegulatoryIngestionFrameworkResponseDto> frameworkById;

		private final Map<UUID, RegulatoryIngestionFileResponseDto> fileById;

		private final Map<UUID, RegulatoryIngestionRequirementResponseDto> requirementById;

		private final Map<UUID, ControlApiControlGroupResponseDto> groupById;

		private final Map<UUID, ControlApiControlResponseDto> controlById;

		private final Map<UUID, List<RegulatoryIngestionFrameworkResponseDto>> frameworksByAuthority;

		private final Map<UUID, List<RegulatoryIngestionFileResponseDto>> filesByFramework;

		private final Map<UUID, List<RegulatoryIngestionRequirementResponseDto>> requirementsByFile;

		private final Map<UUID, List<RegulatoryIngestionRequirementResponseDto>> requirementsByParent;

		private final Map<UUID, List<ControlApiControlGroupResponseDto>> groupsByParent;

		private final Map<UUID, List<ControlApiControlResponseDto>> controlsByGroup;

		private final Map<UUID, List<ControlApiControlResponseDto>> controlsByParent;

		private final Set<UUID> parentRequirementIds;

		private final Map<UUID, List<ControlMappingApiMappingResponseDto>> mappingsByRequirement;

		private final Map<UUID, List<ControlMappingApiMappingResponseDto>> mappingsByControl;

		private Assembly(List<RegulatoryAuthorityResponseDto> authorities,
				List<RegulatoryIngestionFrameworkResponseDto> frameworks, List<RegulatoryIngestionFileResponseDto> files,
				List<RegulatoryIngestionRequirementResponseDto> requirements,
				List<ControlApiControlGroupResponseDto> groups, List<ControlApiControlResponseDto> controls,
				List<ControlMappingApiMappingResponseDto> mappings) {
			this.authorities = authorities;
			this.groups = groups;
			this.controls = controls;
			authorityById = byId(authorities, RegulatoryAuthorityResponseDto::id);
			frameworkById = byId(frameworks, RegulatoryIngestionFrameworkResponseDto::id);
			fileById = byId(files, RegulatoryIngestionFileResponseDto::id);
			requirementById = byId(requirements, RegulatoryIngestionRequirementResponseDto::id);
			groupById = byId(groups, ControlApiControlGroupResponseDto::id);
			controlById = byId(controls, ControlApiControlResponseDto::id);
			frameworksByAuthority = groupedBy(frameworks, RegulatoryIngestionFrameworkResponseDto::authorityId,
					framework -> framework.authorityId() != null, FRAMEWORK_ORDER);
			filesByFramework = groupedBy(files, RegulatoryIngestionFileResponseDto::frameworkId,
					file -> file.frameworkId() != null, FILE_ORDER);
			requirementsByFile = groupedBy(requirements, RegulatoryIngestionRequirementResponseDto::fileId,
					requirement -> requirement.fileId() != null, REQUIREMENT_ORDER);
			requirementsByParent = groupedBy(requirements,
					RegulatoryIngestionRequirementResponseDto::parentRequirementId,
					requirement -> requirementById.containsKey(requirement.parentRequirementId()), REQUIREMENT_ORDER);
			groupsByParent = groupedBy(groups, ControlApiControlGroupResponseDto::parentGroupId,
					group -> groupById.containsKey(group.parentGroupId()), GROUP_ORDER);
			controlsByGroup = groupedBy(controls, ControlApiControlResponseDto::groupId,
					control -> !isEnhancement(control) && groupById.containsKey(control.groupId()), CONTROL_ORDER);
			controlsByParent = groupedBy(controls, ControlApiControlResponseDto::parentControlId, this::isEnhancement,
					CONTROL_ORDER);
			parentRequirementIds = requirementsByParent.keySet();
			List<ControlMappingApiMappingResponseDto> placedMappings = mappings.stream()
				.filter(mapping -> isPlaced(mapping.requirementId()) && controlById.containsKey(mapping.controlId()))
				.toList();
			mappingsByRequirement = placedMappings.stream()
				.collect(Collectors.groupingBy(ControlMappingApiMappingResponseDto::requirementId));
			mappingsByControl = placedMappings.stream()
				.collect(Collectors.groupingBy(ControlMappingApiMappingResponseDto::controlId));
		}

		private DashboardResponseDto dashboard() {
			return new DashboardResponseDto(authorities.stream().sorted(AUTHORITY_ORDER).map(this::authority).toList(),
					groups.stream()
						.filter(group -> !groupById.containsKey(group.parentGroupId()))
						.sorted(GROUP_ORDER)
						.map(this::group)
						.toList(),
					controls.stream()
						.filter(control -> !isEnhancement(control) && !groupById.containsKey(control.groupId()))
						.sorted(CONTROL_ORDER)
						.map(this::control)
						.toList());
		}

		private DashboardAuthorityResponseDto authority(RegulatoryAuthorityResponseDto authority) {
			return new DashboardAuthorityResponseDto(authority.id(), authority.name(), authority.jurisdiction(),
					authority.enabled(), authority.createdAt(), authority.createdBy(),
					children(frameworksByAuthority, authority.id()).stream().map(this::framework).toList());
		}

		private DashboardFrameworkResponseDto framework(RegulatoryIngestionFrameworkResponseDto framework) {
			List<DashboardFileResponseDto> files = children(filesByFramework, framework.id()).stream()
				.map(this::file)
				.toList();
			DashboardCoverageResponseDto coverage = new DashboardCoverageResponseDto(
					files.stream().mapToInt(file -> file.coverage().mapped()).sum(),
					files.stream().mapToInt(file -> file.coverage().total()).sum());
			return new DashboardFrameworkResponseDto(framework.id(), framework.name(), framework.shortName(),
					framework.citation(), framework.enabled(), framework.createdAt(), framework.createdBy(), coverage,
					files);
		}

		private DashboardFileResponseDto file(RegulatoryIngestionFileResponseDto file) {
			List<RegulatoryIngestionRequirementResponseDto> requirements = children(requirementsByFile, file.id());
			List<UUID> leafIds = requirements.stream()
				.map(RegulatoryIngestionRequirementResponseDto::id)
				.filter(id -> !parentRequirementIds.contains(id))
				.toList();
			DashboardCoverageResponseDto coverage = new DashboardCoverageResponseDto(
					(int) leafIds.stream().filter(mappingsByRequirement::containsKey).count(), leafIds.size());
			return new DashboardFileResponseDto(file.id(), file.title(), file.sourceUrl(), file.versionLabel(),
					file.publicationDate(), file.createdAt(), file.createdBy(), coverage,
					requirements.stream()
						.filter(requirement -> !requirementById.containsKey(requirement.parentRequirementId()))
						.map(this::requirement)
						.toList());
		}

		private DashboardRequirementResponseDto requirement(RegulatoryIngestionRequirementResponseDto requirement) {
			return new DashboardRequirementResponseDto(requirement.id(), requirement.provisionRef(),
					requirement.heading(), requirement.text(), requirement.createdAt(), requirement.createdBy(),
					children(mappingsByRequirement, requirement.id()).stream()
						.map(this::mappedControl)
						.sorted(MAPPED_CONTROL_ORDER)
						.toList(),
					children(requirementsByParent, requirement.id()).stream().map(this::requirement).toList());
		}

		private DashboardMappedControlResponseDto mappedControl(ControlMappingApiMappingResponseDto mapping) {
			ControlApiControlResponseDto control = controlById.get(mapping.controlId());
			return new DashboardMappedControlResponseDto(mapping.id(), control.id(), control.controlRef(),
					control.title(), mapping.rationale());
		}

		private DashboardControlGroupResponseDto group(ControlApiControlGroupResponseDto group) {
			return new DashboardControlGroupResponseDto(group.id(), group.groupRef(), group.title(), group.createdAt(),
					group.createdBy(), children(controlsByGroup, group.id()).stream().map(this::control).toList(),
					children(groupsByParent, group.id()).stream().map(this::group).toList());
		}

		private DashboardControlResponseDto control(ControlApiControlResponseDto control) {
			return new DashboardControlResponseDto(control.id(), control.controlRef(), control.title(),
					control.statement(), control.createdAt(), control.createdBy(),
					children(mappingsByControl, control.id()).stream()
						.map(this::mappedRequirement)
						.sorted(MAPPED_REQUIREMENT_ORDER)
						.toList(),
					children(controlsByParent, control.id()).stream().map(this::control).toList());
		}

		private DashboardMappedRequirementResponseDto mappedRequirement(ControlMappingApiMappingResponseDto mapping) {
			RegulatoryIngestionRequirementResponseDto requirement = requirementById.get(mapping.requirementId());
			RegulatoryIngestionFileResponseDto file = fileById.get(requirement.fileId());
			RegulatoryIngestionFrameworkResponseDto framework = frameworkById.get(file.frameworkId());
			RegulatoryAuthorityResponseDto authority = authorityById.get(framework.authorityId());
			return new DashboardMappedRequirementResponseDto(mapping.id(), requirement.id(), requirement.provisionRef(),
					requirement.heading(), file.id(), file.title(), framework.id(), framework.name(), authority.id(),
					authority.name(), mapping.rationale());
		}

		private boolean isPlaced(UUID requirementId) {
			RegulatoryIngestionRequirementResponseDto requirement = requirementById.get(requirementId);
			RegulatoryIngestionFileResponseDto file = requirement == null ? null : fileById.get(requirement.fileId());
			RegulatoryIngestionFrameworkResponseDto framework = file == null ? null
					: frameworkById.get(file.frameworkId());
			return framework != null && authorityById.containsKey(framework.authorityId());
		}

		private boolean isEnhancement(ControlApiControlResponseDto control) {
			return controlById.containsKey(control.parentControlId());
		}

	}

}
