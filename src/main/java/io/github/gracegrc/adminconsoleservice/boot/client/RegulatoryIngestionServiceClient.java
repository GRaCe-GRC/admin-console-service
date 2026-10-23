package io.github.gracegrc.adminconsoleservice.boot.client;

import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionAuthorityResponseDto;
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
		return idFrom(location);
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

	private static UUID idFrom(URI location) {
		if (location == null) {
			throw new RestClientException(
					"The regulatory ingestion service did not return the location of the new authority");
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
