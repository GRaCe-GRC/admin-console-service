package io.github.gracegrc.adminconsoleservice.domain.controller;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.service.DashboardService;
import io.github.gracegrc.adminconsoleservice.service.regulatory.RegulatoryIngestionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/dashboard")
public class AdminConsoleServiceController {

	private final RegulatoryIngestionService regulatoryIngestionService;

	private final DashboardService dashboardService;

	public AdminConsoleServiceController(RegulatoryIngestionService regulatoryIngestionService,
			DashboardService dashboardService) {
		this.regulatoryIngestionService = regulatoryIngestionService;
		this.dashboardService = dashboardService;
	}

	@GetMapping
	public DashboardResponseDto findDashboard() {
		return dashboardService.findDashboard();
	}

	@GetMapping("/authorities")
	public List<RegulatoryAuthorityResponseDto> findAll() {
		return regulatoryIngestionService.findAllAuthorities();
	}

	@GetMapping("/authorities/{id}")
	public RegulatoryAuthorityResponseDto findById(@PathVariable UUID id) {
		return regulatoryIngestionService.findAuthorityById(id);
	}

	@PostMapping("/authorities")
	public ResponseEntity<Void> create(@Valid @RequestBody RegulatoryAuthorityRequestDto request) {
		UUID id = regulatoryIngestionService.createAuthority(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).build();
	}

	@DeleteMapping("/authorities/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		regulatoryIngestionService.deleteAuthority(id);
	}

	@PostMapping("/frameworks")
	public ResponseEntity<Void> createFramework(@Valid @RequestBody RegulatoryFrameworkRequestDto request) {
		UUID id = regulatoryIngestionService.createFramework(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).build();
	}

	@DeleteMapping("/frameworks/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteFramework(@PathVariable UUID id) {
		regulatoryIngestionService.deleteFramework(id);
	}

}
