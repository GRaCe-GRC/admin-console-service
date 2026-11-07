package io.github.gracegrc.adminconsoleservice.domain.service;

import io.github.gracegrc.adminconsoleservice.domain.mapper.DashboardMapper;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityResponseDto;
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

	private final Executor dashboardExecutor;

	public DashboardServiceImpl(RegulatoryIngestionService regulatoryIngestionService, Executor dashboardExecutor) {
		this.regulatoryIngestionService = regulatoryIngestionService;
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
		return DashboardMapper.toResponseDto(authorities.join(), frameworks.join(), files.join(),
				requirements.join());
	}

	private <T> CompletableFuture<List<T>> fetch(Supplier<List<T>> call) {
		return CompletableFuture.supplyAsync(() -> Objects.requireNonNullElse(call.get(), List.<T>of()),
				dashboardExecutor);
	}

}
