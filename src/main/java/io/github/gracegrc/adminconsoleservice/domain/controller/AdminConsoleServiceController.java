package io.github.gracegrc.adminconsoleservice.domain.controller;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlGroupRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlGroupUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlMappingRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlMappingUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFileRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFileUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFrameworkUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.service.DashboardService;
import io.github.gracegrc.adminconsoleservice.service.control.ControlApiService;
import io.github.gracegrc.adminconsoleservice.service.control.ControlMappingApiService;
import io.github.gracegrc.adminconsoleservice.service.regulatory.RegulatoryApiService;
import io.github.gracegrc.adminconsoleservice.service.regulatory.RegulatoryIngestionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/dashboard")
public class AdminConsoleServiceController {

	private final RegulatoryIngestionService regulatoryIngestionService;

	private final RegulatoryApiService regulatoryApiService;

	private final DashboardService dashboardService;

	private final ControlMappingApiService controlMappingApiService;

	private final ControlApiService controlApiService;

	public AdminConsoleServiceController(RegulatoryIngestionService regulatoryIngestionService,
			RegulatoryApiService regulatoryApiService, DashboardService dashboardService,
			ControlMappingApiService controlMappingApiService, ControlApiService controlApiService) {
		this.regulatoryIngestionService = regulatoryIngestionService;
		this.regulatoryApiService = regulatoryApiService;
		this.dashboardService = dashboardService;
		this.controlMappingApiService = controlMappingApiService;
		this.controlApiService = controlApiService;
	}

	@GetMapping
	public DashboardResponseDto retrieveDashboard() {
		return dashboardService.retrieveDashboard();
	}

	@PostMapping("/authorities")
	public ResponseEntity<Void> create(@Valid @RequestBody RegulatoryAuthorityRequestDto request) {
		UUID id = regulatoryIngestionService.createAuthority(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).build();
	}

	@PutMapping("/authorities/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateAuthority(@PathVariable UUID id,
			@Valid @RequestBody RegulatoryAuthorityUpdateRequestDto request) {
		regulatoryApiService.updateAuthority(id, request);
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

	@PutMapping("/frameworks/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateFramework(@PathVariable UUID id,
			@Valid @RequestBody RegulatoryFrameworkUpdateRequestDto request) {
		regulatoryApiService.updateFramework(id, request);
	}

	@DeleteMapping("/frameworks/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteFramework(@PathVariable UUID id) {
		regulatoryIngestionService.deleteFramework(id);
	}

	@PostMapping("/files")
	public ResponseEntity<Void> createFile(@Valid @RequestBody RegulatoryFileRequestDto request) {
		UUID id = regulatoryIngestionService.createFile(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).build();
	}

	@PutMapping("/files/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateFile(@PathVariable UUID id, @Valid @RequestBody RegulatoryFileUpdateRequestDto request) {
		regulatoryApiService.updateFile(id, request);
	}

	@DeleteMapping("/files/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteFile(@PathVariable UUID id) {
		regulatoryIngestionService.deleteFile(id);
	}

	@PostMapping("/control-groups")
	public ResponseEntity<Void> createControlGroup(@Valid @RequestBody ControlGroupRequestDto request) {
		UUID id = controlApiService.createControlGroup(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).build();
	}

	@PutMapping("/control-groups/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateControlGroup(@PathVariable UUID id, @Valid @RequestBody ControlGroupUpdateRequestDto request) {
		controlApiService.updateControlGroup(id, request);
	}

	@DeleteMapping("/control-groups/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteControlGroup(@PathVariable UUID id) {
		controlApiService.deleteControlGroup(id);
	}

	@PostMapping("/controls")
	public ResponseEntity<Void> createControl(@Valid @RequestBody ControlRequestDto request) {
		UUID id = controlApiService.createControl(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).build();
	}

	@PutMapping("/controls/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateControl(@PathVariable UUID id, @Valid @RequestBody ControlUpdateRequestDto request) {
		controlApiService.updateControl(id, request);
	}

	@DeleteMapping("/controls/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteControl(@PathVariable UUID id) {
		controlApiService.deleteControl(id);
	}


	@PostMapping("/mappings")
	public ResponseEntity<Void> createMapping(@Valid @RequestBody ControlMappingRequestDto request) {
		UUID id = controlMappingApiService.createMapping(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).build();
	}

	@PutMapping("/mappings/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateMapping(@PathVariable UUID id, @Valid @RequestBody ControlMappingUpdateRequestDto request) {
		controlMappingApiService.updateMapping(id, request);
	}

	@DeleteMapping("/mappings/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteMapping(@PathVariable UUID id) {
		controlMappingApiService.deleteMapping(id);
	}

}
