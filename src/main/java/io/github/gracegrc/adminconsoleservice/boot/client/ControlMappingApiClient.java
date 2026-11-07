package io.github.gracegrc.adminconsoleservice.boot.client;

import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingResponseDto;
import java.util.List;
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

	public List<ControlMappingApiMappingResponseDto> findAllMappings() {
		try {
			return controlMappingApiRestClient.get()
				.uri(CONTROL_MAPPINGS_PATH)
				.retrieve()
				.body(new ParameterizedTypeReference<>() {
				});
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

}
