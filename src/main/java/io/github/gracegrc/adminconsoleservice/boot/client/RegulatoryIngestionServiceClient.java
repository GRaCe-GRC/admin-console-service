package io.github.gracegrc.adminconsoleservice.boot.client;

import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionAuthorityResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFileResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFrameworkRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFrameworkResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionRequirementResponseDto;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RegulatoryIngestionServiceClient {

	private static final String AUTHORITIES_PATH = "/api/v1/authorities";

	private static final String FRAMEWORKS_PATH = "/api/v1/frameworks";

	private static final String FILES_PATH = "/api/v1/files";

	private static final String REQUIREMENTS_PATH = "/api/v1/requirements";

	private final RestClient regulatoryIngestionServiceRestClient;

	public RegulatoryIngestionServiceClient(RestClient regulatoryIngestionServiceRestClient) {
		this.regulatoryIngestionServiceRestClient = regulatoryIngestionServiceRestClient;
	}

	public UUID createAuthority(RegulatoryIngestionAuthorityRequestDto request) {
		URI location = regulatoryIngestionServiceRestClient.post()
			.uri(AUTHORITIES_PATH)
			.body(request)
			.retrieve()
			.toBodilessEntity()
			.getHeaders()
			.getLocation();
		return idFrom(location, "authority");
	}

	public List<RegulatoryIngestionAuthorityResponseDto> findAllAuthorities() {
		return regulatoryIngestionServiceRestClient.get()
			.uri(AUTHORITIES_PATH)
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});
	}

	public RegulatoryIngestionAuthorityResponseDto findAuthorityById(UUID id) {
		return regulatoryIngestionServiceRestClient.get()
			.uri(AUTHORITIES_PATH + "/{id}", id)
			.retrieve()
			.body(RegulatoryIngestionAuthorityResponseDto.class);
	}

	public void deleteAuthority(UUID id) {
		regulatoryIngestionServiceRestClient.delete().uri(AUTHORITIES_PATH + "/{id}", id).retrieve().toBodilessEntity();
	}

	public UUID createFramework(RegulatoryIngestionFrameworkRequestDto request) {
		URI location = regulatoryIngestionServiceRestClient.post()
			.uri(FRAMEWORKS_PATH)
			.body(request)
			.retrieve()
			.toBodilessEntity()
			.getHeaders()
			.getLocation();
		return idFrom(location, "framework");
	}

	public List<RegulatoryIngestionFrameworkResponseDto> findAllFrameworks() {
		return regulatoryIngestionServiceRestClient.get()
			.uri(FRAMEWORKS_PATH)
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});
	}

	public void deleteFramework(UUID id) {
		regulatoryIngestionServiceRestClient.delete().uri(FRAMEWORKS_PATH + "/{id}", id).retrieve().toBodilessEntity();
	}

	public List<RegulatoryIngestionFileResponseDto> findAllFiles() {
		return regulatoryIngestionServiceRestClient.get()
			.uri(FILES_PATH)
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});
	}

	public List<RegulatoryIngestionRequirementResponseDto> findAllRequirements() {
		return regulatoryIngestionServiceRestClient.get()
			.uri(REQUIREMENTS_PATH)
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});
	}

	private static UUID idFrom(URI location, String resource) {
		if (location == null) {
			throw new RestClientException(
					"The regulatory ingestion service did not return the location of the new " + resource);
		}
		String path = location.getPath();
		try {
			return UUID.fromString(path.substring(path.lastIndexOf('/') + 1));
		}
		catch (IllegalArgumentException ex) {
			throw new RestClientException("The regulatory ingestion service returned an invalid location: " + location,
					ex);
		}
	}

}
