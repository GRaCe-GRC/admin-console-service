package io.github.gracegrc.adminconsoleservice.boot.client;

import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingUpdateRequestDto;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class ControlMappingApiClient {

	private static final String CONTROL_MAPPINGS_PATH = "/api/v1/control-mappings";

	private final RestClient controlMappingApiRestClient;

	public ControlMappingApiClient(RestClient controlMappingApiRestClient) {
		this.controlMappingApiRestClient = controlMappingApiRestClient;
	}

	public UUID createMapping(ControlMappingApiMappingRequestDto request) {
		URI location = call(() -> controlMappingApiRestClient.post()
			.uri(CONTROL_MAPPINGS_PATH)
			.body(request)
			.retrieve()
			.toBodilessEntity()
			.getHeaders()
			.getLocation());
		return idFrom(location);
	}

	public List<ControlMappingApiMappingResponseDto> findAllMappings() {
		return call(() -> controlMappingApiRestClient.get()
			.uri(CONTROL_MAPPINGS_PATH)
			.retrieve()
			.body(new ParameterizedTypeReference<List<ControlMappingApiMappingResponseDto>>() {
			}));
	}

	public void updateMapping(UUID id, ControlMappingApiMappingUpdateRequestDto request) {
		call(() -> controlMappingApiRestClient.put()
			.uri(CONTROL_MAPPINGS_PATH + "/{id}", id)
			.body(request)
			.retrieve()
			.toBodilessEntity());
	}

	public void deleteMapping(UUID id) {
		call(() -> controlMappingApiRestClient.delete()
			.uri(CONTROL_MAPPINGS_PATH + "/{id}", id)
			.retrieve()
			.toBodilessEntity());
	}

	private static <T> T call(Supplier<T> request) {
		try {
			return request.get();
		}
		catch (RestClientResponseException ex) {
			if (ex.getStatusCode().is4xxClientError()) {
				throw ex;
			}
			throw new RestClientException("The control mapping API returned " + ex.getStatusCode().value(), ex);
		}
		catch (ResourceAccessException ex) {
			throw new RestClientException("The control mapping API is unavailable", ex);
		}
	}

	private static UUID idFrom(URI location) {
		if (location == null) {
			throw new RestClientException("The control mapping API did not return the location of the new mapping");
		}
		String path = location.getPath();
		try {
			return UUID.fromString(path.substring(path.lastIndexOf('/') + 1));
		}
		catch (IllegalArgumentException ex) {
			throw new RestClientException("The control mapping API returned an invalid location: " + location, ex);
		}
	}

}
