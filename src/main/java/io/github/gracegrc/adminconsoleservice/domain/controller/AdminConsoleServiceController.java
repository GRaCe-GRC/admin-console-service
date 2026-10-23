package io.github.gracegrc.adminconsoleservice.domain.controller;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityResponseDto;
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
@RequestMapping("/api/dashboard/authorities")
public class AdminConsoleServiceController {

	private final RegulatoryIngestionService regulatoryIngestionService;

	public AdminConsoleServiceController(RegulatoryIngestionService regulatoryIngestionService) {
		this.regulatoryIngestionService = regulatoryIngestionService;
	}

	@GetMapping
	public List<RegulatoryAuthorityResponseDto> findAll() {
		return regulatoryIngestionService.findAllAuthorities();
	}

	@GetMapping("/{id}")
	public RegulatoryAuthorityResponseDto findById(@PathVariable UUID id) {
		return regulatoryIngestionService.findAuthorityById(id);
	}

	@PostMapping
	public ResponseEntity<Void> create(@Valid @RequestBody RegulatoryAuthorityRequestDto request) {
		UUID id = regulatoryIngestionService.createAuthority(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).build();
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		regulatoryIngestionService.deleteAuthority(id);
	}

}
