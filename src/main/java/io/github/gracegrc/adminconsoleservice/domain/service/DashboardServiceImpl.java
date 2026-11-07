package io.github.gracegrc.adminconsoleservice.domain.service;

import io.github.gracegrc.adminconsoleservice.domain.mapper.DashboardMapper;
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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

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
	public DashboardResponseDto retrieveDashboard() {
		CompletableFuture<List<RegulatoryAuthorityResponseDto>> authorities = fetch(
				regulatoryIngestionService::findAllAuthorities);
		CompletableFuture<List<RegulatoryIngestionFrameworkResponseDto>> frameworks = fetch(
				regulatoryIngestionService::findAllFrameworks);
		CompletableFuture<List<RegulatoryIngestionFileResponseDto>> files = fetch(regulatoryIngestionService::findAllFiles);
		CompletableFuture<List<RegulatoryIngestionRequirementResponseDto>> requirements = fetch(
				regulatoryIngestionService::findAllRequirements);
		CompletableFuture<List<ControlMappingApiMappingResponseDto>> mappings = fetch(
				controlMappingApiService::findAllMappings);
		CompletableFuture<List<ControlApiControlGroupResponseDto>> groups = fetch(controlApiService::findAllControlGroups);
		CompletableFuture<List<ControlApiControlResponseDto>> controls = fetch(controlApiService::findAllControls);
		return DashboardMapper.toResponseDto(authorities.join(), frameworks.join(), files.join(),
				requirements.join(), mappings.join(), groups.join(), controls.join());
	}

	private <T> CompletableFuture<List<T>> fetch(Supplier<List<T>> call) {
		return CompletableFuture.supplyAsync(() -> Objects.requireNonNullElse(call.get(), List.<T>of()),
				dashboardExecutor);
	}

}
