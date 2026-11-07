package io.github.gracegrc.adminconsoleservice.boot.client;

import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlResponseDto;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class ControlApiClient {

	private static final String CONTROL_GROUPS_PATH = "/api/v1/control-groups";

	private static final String CONTROLS_PATH = "/api/v1/controls";

	private final RestClient controlApiRestClient;

	public ControlApiClient(RestClient controlApiRestClient) {
		this.controlApiRestClient = controlApiRestClient;
	}

	public List<ControlApiControlGroupResponseDto> findAllControlGroups() {
		return call(() -> controlApiRestClient.get()
			.uri(CONTROL_GROUPS_PATH)
			.retrieve()
			.body(new ParameterizedTypeReference<List<ControlApiControlGroupResponseDto>>() {
			}));
	}

	public List<ControlApiControlResponseDto> findAllControls() {
		return call(() -> controlApiRestClient.get()
			.uri(CONTROLS_PATH)
			.retrieve()
			.body(new ParameterizedTypeReference<List<ControlApiControlResponseDto>>() {
			}));
	}

	private static <T> T call(Supplier<T> request) {
		try {
			return request.get();
		}
		catch (RestClientResponseException ex) {
			if (ex.getStatusCode().is4xxClientError()) {
				throw ex;
			}
			throw new RestClientException("The control API returned " + ex.getStatusCode().value(), ex);
		}
		catch (ResourceAccessException ex) {
			throw new RestClientException("The control API is unavailable", ex);
		}
	}

}
